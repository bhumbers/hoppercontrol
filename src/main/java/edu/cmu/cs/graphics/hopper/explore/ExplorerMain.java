package edu.cmu.cs.graphics.hopper.explore;

import org.apache.log4j.xml.DOMConfigurator;
import org.box2d.proto.Box2D;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.serialization.pb.PbDeserializer;
import org.jbox2d.serialization.pb.PbSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExplorerMain {

    private static final Logger log = LoggerFactory.getLogger(ExplorerMain.class);

    public static void main(String[] args) {
        DOMConfigurator.configure("config/log4j.xml");

        Explorer explorer = new SimpleExplorer();
        explorer.explore();

        Vec2 gravity = new Vec2(0, -10f);
        World world = new World(gravity);
        PbSerializer serializer = new PbSerializer();
        PbDeserializer deserializer = new PbDeserializer();
        Box2D.PbWorld serializedWorld =  serializer.serializeWorld(world).build();
        World world2 = deserializer.deserializeWorld(serializedWorld);

        log.debug("Things and stuff");
        log.info("Things and stuff");
        System.out.println(world2.toString());
    }
}
