package edu.cmu.cs.graphics.hopper.eval;

/**
 * Value stored in eval caches
 */
public final class EvalCacheValue {
    public final Evaluator.Status status;

    public EvalCacheValue(Evaluator.Status status) {
        this.status = status;
    }
}
