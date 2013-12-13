package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.BipedHopperDefinition;
import edu.cmu.cs.graphics.hopper.eval.BipedObstacleEvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.eval.EvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.io.IOUtils;
import edu.cmu.cs.graphics.hopper.oracle.AssociativeOracle;
import edu.cmu.cs.graphics.hopper.oracle.ChallengeOracle;
import edu.cmu.cs.graphics.hopper.oracle.UserOracle;
import edu.cmu.cs.graphics.hopper.problems.ObstacleProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.TerrainProblemDefinition;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.xml.DOMConfigurator;
import org.box2d.proto.Box2D;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.serialization.pb.PbDeserializer;
import org.jbox2d.serialization.pb.PbSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

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
        String autoOracleSolsPath = config.getString("autoOracleSolsPath");
        boolean saveSols = config.getBoolean("saveSolutions");
        boolean saveLog = config.getBoolean("saveExplorationLog");
        boolean verifyOracleSols = config.getBoolean("verifyOracleSolutions");

        String saveSolsDir = explorationOutputPath + explorationName + "/sols/";
        String saveLogDir = explorationOutputPath + explorationName + "/";

        int numProblems = config.getInt("numProblems");
        int terrainSeed = config.getInt("terrainSeed");
        int terrainLength = config.getInt("terrainLength");
        float terrainDeltaX = config.getFloat("terrainDeltaX");
        float terrainMaxAmp = config.getFloat("terrainMaxAmp");

        log.info("Starting a control exploration named " + explorationName);

        List<ProblemDefinition> problems = new ArrayList<ProblemDefinition>();

        //Terrain test
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

        //Test problem set
//        for (int i = 1; i < 2; i++) {
//            for (int j = 1; j < 2; j++) {
//                problems.add(new ObstacleProblemDefinition(i,j));
//            }
//        }

        //Test avatar
        AvatarDefinition avatarDef = new BipedHopperDefinition();

        List<ChallengeOracle<BipedHopperControl>> oracles = new ArrayList<ChallengeOracle<BipedHopperControl>>();

        //Automated oracle
        AssociativeOracle<BipedHopperControl> autoOracle = new AssociativeOracle<BipedHopperControl>();
        List<ProblemSolutionEntry> solutionEntries = IOUtils.instance().loadAllProblemSolutionEntriesInDir(autoOracleSolsPath);
        for (ProblemSolutionEntry solutionEntry : solutionEntries)
            autoOracle.addSolutionEntry(solutionEntry.problem, solutionEntry.solution);
        oracles.add(autoOracle);

        //User oracle
        UserOracle<BipedHopperControl> userOracle = new UserOracle<BipedHopperControl>();
        oracles.add(userOracle);

        //Test evaluation
        float minXForSuccess = terrainLength * terrainDeltaX;
        EvaluatorDefinition evalDef = new BipedObstacleEvaluatorDefinition(30.0f, minXForSuccess, 1.0f, 3.0f);

        Explorer explorer = new SimpleExplorer(); //TODO: Make this configurable
        explorer.setSolutionsSaved(saveSols);
        explorer.setSolutionsSavePath(saveSolsDir);
        explorer.setLogSaved(saveLog);
        explorer.setLogSavePath(saveLogDir);
        explorer.setVerifyOracleSols(verifyOracleSols);
        explorer.explore(problems, avatarDef, evalDef, oracles);

        log.info("Control exploration COMPLETE");
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
