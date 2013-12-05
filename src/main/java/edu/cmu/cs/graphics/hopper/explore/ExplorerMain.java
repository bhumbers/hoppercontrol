package edu.cmu.cs.graphics.hopper.explore;

import com.thoughtworks.xstream.XStream;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.oracle.ChallengeOracle;
import edu.cmu.cs.graphics.hopper.oracle.UserOracle;
import edu.cmu.cs.graphics.hopper.problems.ObstacleProblem;
import edu.cmu.cs.graphics.hopper.problems.Problem;
import edu.cmu.cs.graphics.hopper.problems.TerrainProblem;
import org.apache.log4j.xml.DOMConfigurator;
import org.box2d.proto.Box2D;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.serialization.pb.PbDeserializer;
import org.jbox2d.serialization.pb.PbSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExplorerMain {

    private static final Logger log = LoggerFactory.getLogger(ExplorerMain.class);

    public static void main(String[] args) {
        DOMConfigurator.configure("config/log4j.xml");

        log.info("Starting a control exploration...");

        //Test problem set
        List<Problem> problems = new ArrayList<Problem>();
        problems.add(new ObstacleProblem(1,1));

        //Test oracle
        ChallengeOracle<BipedHopperControl> oracle = new UserOracle<BipedHopperControl>();

        Explorer explorer = new SimpleExplorer();
        explorer.explore(problems, oracle);

        //Box2D world serialization/deserialization test
//        Vec2 gravity = new Vec2(0, -10f);
//        World world = new World(gravity);
//        PbSerializer serializer = new PbSerializer();
//        PbDeserializer deserializer = new PbDeserializer();
//        Box2D.PbWorld serializedWorld =  serializer.serializeWorld(world).build();
//        World world2 = deserializer.deserializeWorld(serializedWorld);
//        System.out.println(world2.toString());


        log.info("Control exploration COMPLETE");
    }
}
