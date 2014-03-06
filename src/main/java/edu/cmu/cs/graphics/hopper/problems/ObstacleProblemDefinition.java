package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.*;

/** A solo obstacle clearing problem for avatar */
public final class ObstacleProblemDefinition extends ProblemDefinition {
     //Definition
    final protected float width;
    final protected float height;

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

        Body body = world.createBody(bd);
        body.createFixture(fd);
    }

    @Override
    public String toString() {
        return "ObstacleProbDef: W=" + width + ", H=" + height;
    }

    @Override
    public double[] getParamsArray() {
        return new double[]{width, height};
    }

    @Override
    public Object getState() {
        //TODO
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ObstacleProblemDefinition that = (ObstacleProblemDefinition) o;

        if (Float.compare(that.height, height) != 0) return false;
        if (Float.compare(that.width, width) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (width != +0.0f ? Float.floatToIntBits(width) : 0);
        result = 31 * result + (height != +0.0f ? Float.floatToIntBits(height) : 0);
        return result;
    }
}
