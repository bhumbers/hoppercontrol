package edu.cmu.cs.graphics.hopper;

import org.jbox2d.common.Vec2;

public class VecUtils {


    /** Rotates given vector by specified angle  (in radians) and returns as a new vector */
    public static Vec2 rotate(Vec2 a, float angle) {
        Vec2 aRot = a.clone();
        return rotateLocal(aRot, angle);
    }

    /** Rotates given vector by specified angle (in radians) and sets vector to rotated result */
    public static Vec2 rotateLocal(Vec2 a, float angle) {
        float s = org.jbox2d.common.MathUtils.sin(angle);
        float c = org.jbox2d.common.MathUtils.cos(angle);
        float x = c*a.x - s*a.y;
        float y = s*a.x + c*a.y;
        a.x = x; a.y = y;
        return a;
    }
}
