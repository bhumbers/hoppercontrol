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
import org.apache.log4j.xml.DOMConfigurator;
import org.box2d.proto.Box2D;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.serialization.pb.PbDeserializer;
import org.jbox2d.serialization.pb.PbSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class ExplorerMain {

    private static final Logger log = LoggerFactory.getLogger(ExplorerMain.class);

    public static void main(String[] args) {
        DOMConfigurator.configure("config/log4j.xml");

        log.info("Starting a control exploration...");

        String explorationName = "Columbus";
        String autoOracleSolsDir = "data/sols2/";
        boolean saveSols = true;
        String saveSolsDir = "exploration/" + explorationName + "/sols/";
        boolean saveLog = true;
        String saveLogDir = "exploration/" + explorationName + "/";

        List<ProblemDefinition> problems = new ArrayList<ProblemDefinition>();

        //Terrain test
        int terrainLength = 3;
        float terrainDeltaX = 2.0f;
        float terrainMaxAmp = 2.0f;
        Random r = new Random();
        r.setSeed(12345);
        for (int i = 0; i < 200; i++) {

            float y = 0.0f;
            List<Float> verts = new ArrayList<Float>(terrainLength);
            verts.add(0.01f);
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
        List<ProblemSolutionEntry> solutionEntries = IOUtils.instance().loadAllProblemSolutionEntriesInDir(autoOracleSolsDir);
        for (ProblemSolutionEntry solutionEntry : solutionEntries)
            autoOracle.addSolutionEntry(solutionEntry.problem, solutionEntry.solution);
        oracles.add(autoOracle);

        //User oracle
        UserOracle<BipedHopperControl> userOracle = new UserOracle<BipedHopperControl>();
        oracles.add(userOracle);

        //Test evaluation
        float minXForSuccess = terrainLength * terrainDeltaX;
        EvaluatorDefinition evalDef = new BipedObstacleEvaluatorDefinition(30.0f, minXForSuccess, 1.0f, 3.0f);

        Explorer explorer = new SimpleExplorer();
        explorer.setSolutionsSaved(saveSols);
        explorer.setSolutionsSavePath(saveSolsDir);
        explorer.setLogSaved(saveLog);
        explorer.setLogSavePath(saveLogDir);
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
