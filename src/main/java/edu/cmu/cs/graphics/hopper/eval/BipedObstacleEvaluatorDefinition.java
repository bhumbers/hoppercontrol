package edu.cmu.cs.graphics.hopper.eval;

/** Creates an evaluator that judges success/fitness based on obstacle-clearing behavior of a biped hopper */
public final class BipedObstacleEvaluatorDefinition extends EvaluatorDefinition {
    public final float maxTime;                                     //sim time (in seconds) before failure
    public final float minXForSuccess;                              //x position which avatar must reach for success
    public final float maxUprightDeviation;                        //max angular deviation (in radians) away from vertical before avatar is not considered upright
    public final float minConsecutiveUprightTimeAfterMinXReached;      //time (in seconds) which must pass while hopper is "upright" after reaching min x before success

    public BipedObstacleEvaluatorDefinition(float maxTime,
                                            float minXForSuccess,
                                            float maxUprightDeviation,
                                            float minConsecutiveUprightTimeAfterMinXReached)
    {
        this.maxTime = maxTime;
        this.minXForSuccess = minXForSuccess;
        this.maxUprightDeviation = maxUprightDeviation;
        this.minConsecutiveUprightTimeAfterMinXReached = minConsecutiveUprightTimeAfterMinXReached;
    }

    @Override
    public Evaluator create() {
        return new BipedObstacleEvaluator(this);
    }
}
