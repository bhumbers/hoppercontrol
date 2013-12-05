package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.BipedHopperDefinition;
import edu.cmu.cs.graphics.hopper.oracle.ChallengeOracle;
import edu.cmu.cs.graphics.hopper.oracle.UserOracle;
import edu.cmu.cs.graphics.hopper.problems.ObstacleProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ExplorerMain {

    private static final Logger log = LoggerFactory.getLogger(ExplorerMain.class);

    public static void main(String[] args) {
        DOMConfigurator.configure("config/log4j.xml");

        log.info("Starting a control exploration...");

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
        for (int i = 1; i < 10; i++) {
            for (int j = 1; j < 10; j++) {
                problems.add(new ObstacleProblemDefinition(i,j));
            }
        }

        //Test avatar
        AvatarDefinition avatarDef = new BipedHopperDefinition();

        //Test oracle
        ChallengeOracle<BipedHopperControl> oracle = new UserOracle<BipedHopperControl>();

        Explorer explorer = new SimpleExplorer();
        explorer.explore(problems, avatarDef, oracle);

        //Box2D world serialization/deserialization test
//        Vec2 gravity = new Vec2(0, -10f);
//        World world = new World(gravity);
//        PbSerializer serializer = new PbSerializer();
//        PbDeserializer deserializer = new PbDeserializer();
//        Box2D.PbWorld serializedWorld =  serializer.serializeWorld(world).build();
//        World world2 = deserializer.deserializeWorld(serializedWorld);
//        System.out.println(world2.toString());


        log.info("Control exploration COMPLETE");
        log.info("Problems solved:          " + explorer.getNumSolvedProblems() + "/" + explorer.getNumProblems());
        log.info("Sim Tests used:           " + explorer.getNumTests());
        log.info("Oracle Challenges issued: " + explorer.getNumOracleChallenges());
    }
}
