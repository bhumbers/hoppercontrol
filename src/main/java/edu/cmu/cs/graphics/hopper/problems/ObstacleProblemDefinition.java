package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.*;

/** A solo obstacle clearing problem for avatar */
public class ObstacleProblemDefinition extends ProblemDefinition {
     //Definition
    final protected float width;
    final protected float height;

    protected Body body;

    public ObstacleProblemDefinition(float width, float height) {
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
}
