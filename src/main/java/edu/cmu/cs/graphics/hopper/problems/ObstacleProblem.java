package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import org.jbox2d.collision.shapes.ChainShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

 /** A solo obstacle clearing problem for avatar */
public class ObstacleProblem extends Problem {
     //Definition
    final protected float width;
    final protected float height;

    protected Body body;

    public ObstacleProblem(float width, float height) {
        this.width = width; this.height = height;
    }

    @Override
    public void init(World world) {
        BodyDef bd = new BodyDef();
        bd.type = BodyType.STATIC;
        bd.position.set(0.0f, height*0.5f);

        FixtureDef fd = new FixtureDef();
        fd.density = 0.0f;
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width*0.5f, height*0.5f);
        fd.shape = shape;

        body = world.createBody(bd);
        body.createFixture(fd);
    }

     @Override
     public boolean runControlTest(ControlProvider ctrlProvider) {
         //TODO

         return true;
     }
}
