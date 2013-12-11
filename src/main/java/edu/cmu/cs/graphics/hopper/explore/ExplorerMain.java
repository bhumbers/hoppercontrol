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

public class ExplorerMain {

    private static final Logger log = LoggerFactory.getLogger(ExplorerMain.class);

    public static void main(String[] args) {
        DOMConfigurator.configure("config/log4j.xml");

        log.info("Starting a control exploration...");

        String solutionsDir = "data/sols/";

//        //Terrain test
//        Random r = new Random();
//        r.setSeed(12345);
//        int terrainLength = 100;
//        float terrainDeltaX = 2.0f;
//        float terrainMaxAmp = 4.0f;
//        float y = 0.0f;
//        List<Float> verts = new ArrayList<Float>(terrainLength);
//        verts.add(0.01f);
//        for (int i = 0; i < terrainLength; i++) {
//            y = terrainMaxAmp*(r.nextFloat());
//            if (y < 0)
//                y = 0;
//            verts.add(y);
//        }
//        terrain = new TerrainProblemDefinition(verts, terrainDeltaX);

        //Test problem set
        List<ProblemDefinition> problems = new ArrayList<ProblemDefinition>();
        for (int i = 1; i < 2; i++) {
            for (int j = 1; j < 2; j++) {
                problems.add(new ObstacleProblemDefinition(i,j));
            }
        }

        //Test avatar
        AvatarDefinition avatarDef = new BipedHopperDefinition();

        //User oracle
//        ChallengeOracle<BipedHopperControl> oracle = new UserOracle<BipedHopperControl>();

        //TEST: Automated oracle
        AssociativeOracle<BipedHopperControl> oracle = new AssociativeOracle<BipedHopperControl>();
        for (int i = 1; i < 2; i++) {
            String filename = String.format("%04d", i) + ".sol";
            ProblemSolutionEntry entry = IOUtils.instance().loadProblemSolutionEntry(solutionsDir, filename);
            oracle.addSolutionEntry(entry.problem, entry.solution);
        }

        //Test evaluation
        EvaluatorDefinition evalDef = new BipedObstacleEvaluatorDefinition(30.0f, 20.0f, 1.0f, 5.0f);

        Explorer explorer = new SimpleExplorer();
        explorer.explore(problems, avatarDef, evalDef, oracle);

        log.info("Control exploration COMPLETE");
        log.info("Problems solved:          " + explorer.getNumSolvedProblems() + "/" + explorer.getNumProblems());
        log.info("Sim Tests used:           " + explorer.getNumTests());
        log.info("Oracle Challenges issued: " + explorer.getNumOracleChallenges());

        //TEST: Save solution map to files
        log.info("Oracle Challenges issued: " + explorer.getNumOracleChallenges());
        Collection<ProblemSolutionEntry> solvedProblems = explorer.getSolvedProblems();
        int i = 1;
        for (ProblemSolutionEntry solvedProblem : solvedProblems) {
            String filename = String.format("%04d", i) + ".sol";
            IOUtils.instance().saveProblemSolutionEntry(solvedProblem, filePath, filename);
        }
    }
}
