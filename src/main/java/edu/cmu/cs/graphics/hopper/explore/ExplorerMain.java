package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.BipedHopperDefinition;
import edu.cmu.cs.graphics.hopper.eval.BipedObstacleEvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.eval.EvalCache;
import edu.cmu.cs.graphics.hopper.eval.EvalCacheEntry;
import edu.cmu.cs.graphics.hopper.eval.EvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.io.IOUtils;
import edu.cmu.cs.graphics.hopper.net.HopperPlaySnap;
import edu.cmu.cs.graphics.hopper.net.ServerInterface;
import edu.cmu.cs.graphics.hopper.net.SnapServerInterface;
import edu.cmu.cs.graphics.hopper.oracle.AssociativeOracle;
import edu.cmu.cs.graphics.hopper.oracle.ChallengeOracle;
import edu.cmu.cs.graphics.hopper.oracle.UserOracle;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.TerrainProblemDefinition;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ExplorerMain {

    private static final Logger log = LoggerFactory.getLogger(ExplorerMain.class);

    public static void main(String[] args) {
        DOMConfigurator.configure("config/log4j.xml");

        Options options = new Options();
        options.addOption("configFile", true, "Exploration configuration file name/path");

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        }
        catch (ParseException parseError) {
            log.error("Error occurred while parsing command line inputs");
            parseError.printStackTrace();
            return;
        }

        String configFilePath = cmd.getOptionValue("configFile");
        log.info("Loading config file: " + configFilePath);
        Configuration config = null;
        if (configFilePath != null && !configFilePath.isEmpty()) {
            try {
                File configFile = new File(configFilePath);
                String fullFilePath = configFile.getAbsolutePath();
                config = new PropertiesConfiguration(fullFilePath);
            } catch (ConfigurationException e) {
                log.error("Error while trying to load exploration config file: " + configFilePath);
                e.printStackTrace();
                return;
            }
        }
        else {
            log.error("No config file specified! Exiting... ");
            return;
        }

        String explorationName = config.getString("explorationName");
        String explorationOutputPath = config.getString("explorationOutputPath");
        String[] autoOracleSolsPaths = config.getStringArray("autoOracleSolsPath");
        String[] inputCtrlEnsemblePaths = config.getStringArray("inputCtrlEnsemblePath");

        boolean saveSols = config.getBoolean("saveSolutions");
        boolean saveLog = config.getBoolean("saveExplorationLog");
        boolean verifyOracleSols = config.getBoolean("verifyOracleSolutions");
        boolean saveEvals = config.getBoolean("saveEvals");
        boolean saveCtrlEnsemble = config.getBoolean("saveCtrlEnsemble");
        int maxTestsPerProblem = config.getInt("maxTestsPerProblem");

        boolean useEvalCache = config.getBoolean("useEvalCache");
        String[] evalCachePaths = config.getStringArray("evalCachePath");

        boolean useSmartControlOrdering = config.getBoolean("useSmartControlOrdering");

        boolean enableUserOracle = config.getBoolean("enableUserOracle");

        String saveSolsDir = explorationOutputPath + explorationName + "/sols/";
        String saveLogDir = explorationOutputPath + explorationName + "/";
        String saveEvalsDir = explorationOutputPath + explorationName + "/evals/";
        String saveCtrlEnsembleDir = explorationOutputPath + explorationName + "/ensemble/";

        int numProblems = config.getInt("numProblems");
        int terrainSeed = config.getInt("terrainSeed");
        int terrainLength = config.getInt("terrainLength");
        float terrainDeltaX = config.getFloat("terrainDeltaX");
        String[] terrainMaxAmpStrs = config.getStringArray("terrainMaxAmp");
        float[] terrainMaxAmps = new float[terrainMaxAmpStrs.length];
        for (int i = 0; i < terrainMaxAmps.length; i++)
            terrainMaxAmps[i] = Float.parseFloat(terrainMaxAmpStrs[i]);

        log.info("Starting a control exploration named " + explorationName);
        long t0 = System.currentTimeMillis();

        List<ProblemDefinition> problems = new ArrayList<ProblemDefinition>();

        //Terrain test
        for (int ampIdx = 0; ampIdx < terrainMaxAmps.length; ampIdx++) {
            float terrainMaxAmp = terrainMaxAmps[ampIdx];
            Random r = new Random();
            r.setSeed(terrainSeed);
            for (int i = 0; i < numProblems; i++) {
                float y = 0.0f;
                List<Float> verts = new ArrayList<Float>(terrainLength);
                verts.add(0.0f);      //initial "ground" node
                for (int j = 0; j < terrainLength; j++) {
                    y = terrainMaxAmp*(r.nextFloat());
                    if (y < 0)
                        y = 0;
                    verts.add(y);
                }
                problems.add(new TerrainProblemDefinition(verts, terrainDeltaX));
            }
        }

        //Test problem set
//        for (int i = 1; i < 2; i++) {
//            for (int j = 1; j < 2; j++) {
//                problems.add(new ObstacleProblemDefinition(i,j));
//            }
//        }

        //Test avatar
        AvatarDefinition avatarDef = new BipedHopperDefinition();

        //Evaluation cache setup
        EvalCache evalCache = null;
        if (useEvalCache) {
            evalCache = new EvalCache();
            for (String evalCachePath : evalCachePaths) {
                List<EvalCacheEntry> evalCacheEntries = IOUtils.instance().loadAllEvalCacheEntriesInDir(evalCachePath);
                for (EvalCacheEntry entry : evalCacheEntries) {
                    evalCache.insert(entry.key, entry.value);
                }
            }
        }

        List<ChallengeOracle<BipedHopperControl>> oracles = new ArrayList<ChallengeOracle<BipedHopperControl>>();

        //Automated oracle
        AssociativeOracle<BipedHopperControl> autoOracle = new AssociativeOracle<BipedHopperControl>();
        for (String autoOracleSolsPath : autoOracleSolsPaths) {
            List<ProblemSolutionEntry> solutionEntries = IOUtils.instance().loadAllProblemSolutionEntriesInDir(autoOracleSolsPath);
            for (ProblemSolutionEntry solutionEntry : solutionEntries)
                autoOracle.addSolutionEntry(solutionEntry.problem, solutionEntry.solution);
        }
        oracles.add(autoOracle);

        //User oracle
        if (enableUserOracle) {
            UserOracle<BipedHopperControl> userOracle = new UserOracle<BipedHopperControl>();
            oracles.add(userOracle);
        }

        //Test evaluation
        float maxTime = 15.0f;
        float minXForSuccess = terrainLength * terrainDeltaX;
        float maxUprightDeviation = 1.0f;
        float minConsecutiveUprightTimeAfterMinXReached = 3.0f;
        EvaluatorDefinition evalDef = new BipedObstacleEvaluatorDefinition(maxTime, minXForSuccess, maxUprightDeviation, minConsecutiveUprightTimeAfterMinXReached);

        Explorer explorer;
        if (useSmartControlOrdering)
            explorer = new SmartControlExplorer();
        else
            explorer = new SimpleExplorer();

        //Pre-existing ensemble inputs
        for (String inputCtrlEnsemblePath : inputCtrlEnsemblePaths)
            explorer.loadEnsemble(inputCtrlEnsemblePath);

        explorer.setName(explorationName);
        explorer.setSolutionsSaved(saveSols);
        explorer.setSolutionsSavePath(saveSolsDir);
        explorer.setLogSaved(saveLog);
        explorer.setLogSavePath(saveLogDir);
        explorer.setEvalsSaved(saveEvals);
        explorer.setEvalsSavePath(saveEvalsDir);
        explorer.setControlEnsembleSaved(saveCtrlEnsemble);
        explorer.setControlEnsembleSavePath(saveCtrlEnsembleDir);
        explorer.setVerifyOracleSols(verifyOracleSols);
        explorer.setMaxTestsPerProblem(maxTestsPerProblem);
        if (evalCache != null) explorer.setEvalCache(evalCache);

        //Net logging test
        SnapServerInterface server = new SnapServerInterface("gs13099.sp.cs.cmu.edu", 8080);
        explorer.setServerInterface(server);
//        //TEST
//        HopperPlaySnap snap = new HopperPlaySnap();
//        snap.user = "bhumbers";
//        snap.config = new double[]{1, 2, 3, 4, 5};
//        snap.controls = new float[][]{{42, 6 ,7}, {25, 5, 5}};
//        server.sendPlaySnap(snap);

        explorer.explore(problems, avatarDef, evalDef, oracles);

        long t1 = System.currentTimeMillis();
        long explorationRuntime = (t1 - t0);
        String runtimeStr = String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(explorationRuntime),
                TimeUnit.MILLISECONDS.toMinutes(explorationRuntime) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(explorationRuntime)),
                TimeUnit.MILLISECONDS.toSeconds(explorationRuntime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(explorationRuntime)));

        log.info("Control exploration COMPLETE");
        log.info("Runtime: " + runtimeStr);
        log.info("Problems Solved:          " + explorer.getNumSolvedProblems() + "/" + explorer.getNumProblems());
        log.info("Sim Tests Used:           " + explorer.getNumTests());
        log.info("Oracle Challenges Issued: " + explorer.getNumOracleChallenges());
        log.info("Oracle Challenges Failed: " + explorer.getNumFailedProblems());

        //TEST: Save solution map to files
//        Collection<ProblemSolutionEntry> solvedProblems = explorer.getSolvedProblems();
//        int i = 1;
//        for (ProblemSolutionEntry solvedProblem : solvedProblems) {
//            String filename = String.format("%04d", i) + ".sol";
//            IOUtils.instance().saveProblemSolutionEntry(solvedProblem, solutionsDir, filename);
//            i++;
//        }

        for (ChallengeOracle oracle : oracles)
            oracle.close();
    }
}
