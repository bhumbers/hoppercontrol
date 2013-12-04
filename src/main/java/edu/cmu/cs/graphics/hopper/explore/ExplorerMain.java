package edu.cmu.cs.graphics.hopper.explore;

import com.thoughtworks.xstream.XStream;
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

        Explorer explorer = new SimpleExplorer();
        explorer.explore();

        //Box2D world serialization/deserialization test
//        Vec2 gravity = new Vec2(0, -10f);
//        World world = new World(gravity);
//        PbSerializer serializer = new PbSerializer();
//        PbDeserializer deserializer = new PbDeserializer();
//        Box2D.PbWorld serializedWorld =  serializer.serializeWorld(world).build();
//        World world2 = deserializer.deserializeWorld(serializedWorld);
//        System.out.println(world2.toString());

        //Xstream serialization test
        XStream xstream = new XStream();
        xstream.alias("terrainproblem", TerrainProblem.class);
        TerrainProblem blah = new TerrainProblem(new ArrayList<Float>(Arrays.asList(1.0f, 2.0f, 3.0f)), 42);

        log.debug("Blah: " + xstream.toXML(blah));
    }
}
