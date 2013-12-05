package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import org.jbox2d.dynamics.World;

/** An instantiable control problem on which we can evaluate controller performance
 * NOTE: Problems should override the hashCode() method in order to allow simple set management logic on problem definitions
 * (ie: return true if static problem definition fields are the same)
 * TODO: Come to think of it... may want to separate into "ProblemDefinition" for immutable defs and "Problem" for dynamic state to run the problem */
public abstract class Problem {
    public abstract void init(World world);

    /** Runs given control sequence on problem instance and evaluates fitness.
     * Returns true if control successfully clears problem, false otherwise. */
    public abstract boolean runControlTest(ControlProvider ctrlProvider);
}
