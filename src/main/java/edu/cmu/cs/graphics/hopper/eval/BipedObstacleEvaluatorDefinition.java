package edu.cmu.cs.graphics.hopper.eval;

/** Creates an evaluator that judges success/fitness based on obstacle-clearing behavior of a biped hopper */
public final class BipedObstacleEvaluatorDefinition extends EvaluatorDefinition {
    public final float maxTime;             //sim time (in seconds) before failure
    public final float minXForSuccess;      //x position which avatar must reach for success

    public BipedObstacleEvaluatorDefinition(float maxTime, float minXForSuccess) {
        this.maxTime = maxTime;
        this.minXForSuccess = minXForSuccess;
    }

    @Override
    public Evaluator create() {
        return new BipedObstacleEvaluator(maxTime, minXForSuccess);
    }
}
