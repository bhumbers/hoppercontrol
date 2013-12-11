package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import org.jbox2d.dynamics.World;

/** An instantiable control problem on which we can evaluate controller performance
 * NOTE: Problems should override the hashCode() method in order to allow simple set management logic on problem definitions
 * (ie: return true if static problem definition fields are the same) */
public abstract class ProblemDefinition {
    /** Creates various components for this problem definition and adds to given world*/
    public abstract void init(World world);

    /** Returns a numeric array containing unique parameters for this problem.
     * The length of the returned array should be the same for all problems of a given type. */
    public abstract double[] getParamsArray();
 }
