package edu.cmu.cs.graphics.hopper.eval;

import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.ControlPrim;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

/** A control fitness evaluation which depends on forward motion of the avatar */
public class FwdMotionControlEval extends ControlEval {
    protected Avatar avatar;
    protected ProblemDefinition problem;
    protected ControlPrim controlPrim;

    //Fitness evaluation for current run
    protected float fitness;

    @Override
    public Avatar getAvatar() {return avatar;}
    @Override
    public ProblemDefinition getProblem() {return problem;}
    @Override
    public ControlPrim getControlPrim() {return controlPrim;}

    FwdMotionControlEval(Avatar avatar, ProblemDefinition problem, ControlPrim controlPrim) {
        this.avatar = avatar;
        this.problem = problem;
        this.controlPrim = controlPrim;
        fitness = 0.0f;
    }

    @Override
    public float getEvaluation() {
        return fitness;
    }

    @Override
    protected void updateEvaluation() {
        //Anything to do here?
    }

    @Override
    protected void finishEvaluation() {
        //TODO: Set fitness based on distance that avatar moved forward
    }
}
