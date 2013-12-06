package edu.cmu.cs.graphics.hopper.eval;

import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.ControlPrim;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

/** Evaluates the effectiveness of a control strategy for an avatar during simulation on a particular problem instance */
 public abstract class Evaluator {
    public enum Status {
        RUNNING,
        SUCCESS,
        FAILURE
    }

    /** Do any required initial setup for this evaluation */
    public void init() {}

    /** Returns current evaluation status.  */
    public abstract Status getStatus();

    /** Returns current (or, if completed, final) numeric fitness evaluation of control on problem
     * Note that this is just an arbitrary measure which may prove useful; you'll want to use the evaluation
     * status to get a clear "pass/fail" signal for the evaluation.*/
    public abstract float getFitness();

    /** Incrementally modifies evaluation of controller fitness for curent timestep of problem instance */
    public abstract void updateEvaluation(ProblemInstance problem);

    /** Runs any final evaluation of controller fitness (this is called at the end of simulation run) */
    public abstract void finishEvaluation(ProblemInstance problem);
}
