package edu.cmu.cs.graphics.hopper.eval;

/**
 * Single key,value entry in evaluation cache
 */
public final class EvalCacheEntry {
    public final EvalCacheKey key;
    public final EvalCacheValue value;

    public EvalCacheEntry(EvalCacheKey key, EvalCacheValue value) {
        this.key = key;
        this.value = value;
    }
}
