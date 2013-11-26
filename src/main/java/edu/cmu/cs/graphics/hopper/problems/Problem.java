package edu.cmu.cs.graphics.hopper.problems;

import org.jbox2d.dynamics.World;

/** An instantiable control problem on which we can evaluate controller performance */
public abstract class Problem {

    public abstract void init(World world);
}
