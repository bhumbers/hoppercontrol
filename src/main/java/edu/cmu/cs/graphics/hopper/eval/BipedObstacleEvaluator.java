package edu.cmu.cs.graphics.hopper.eval;

import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.BipedHopper;
import edu.cmu.cs.graphics.hopper.control.ControlPrim;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;

/** An evaluator that judges success/fitness based on obstacle-clearing behavior of a biped hopper.
 * Only applicable to problems where biped hopper is used, since we use a bunch of type-specific knowledge about
 * when the hopper meets with various success/failure conditions. */
public class BipedObstacleEvaluator extends Evaluator {
    //Fitness evaluation for current run
    protected float fitness;
    protected Status status;

    protected float maxTime;
    protected float minXForSuccess;

    protected boolean avatarFellOver;

    BipedObstacleEvaluator(float maxTime, float minXForSuccess) {
        this.maxTime = maxTime;
        this.minXForSuccess = minXForSuccess;
    }

    @Override
    public void init() {
        status = Status.RUNNING;
        avatarFellOver = false;
    }

    @Override
    public Status getStatus() { return status; }

    @Override
    public float getFitness() { return fitness; }

    @Override
    public void updateEvaluation(ProblemInstance problem) {
        //If main body touches ground at any point, then we consider that failure
        BipedHopper hopper = (BipedHopper)problem.getAvatar();
        if (!avatarFellOver && hopper.isChassisInGroundContact())
            avatarFellOver = true;

        //TESTING: Solved if we cross some dist to the right, failed if we go left (arbitrary) or run too long
        //TODO: Actually evaluate problem status. Solved? Failed?
        if (avatarFellOver || problem.getSimTime() > maxTime)
            status = Status.FAILURE;
        else if (problem.getAvatar().getMainBody().getPosition().x > minXForSuccess)
            status = Status.SUCCESS;
        else
            status = Status.RUNNING;
    }

    @Override
    public void finishEvaluation(ProblemInstance problem) {
        //TODO: Set fitness based on distance that avatar moved forward
    }
}
