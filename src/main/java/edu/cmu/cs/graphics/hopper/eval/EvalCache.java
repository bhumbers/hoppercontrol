package edu.cmu.cs.graphics.hopper.eval;

import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

import java.util.HashMap;

/**
 * Provides cached evaluation results without having to run the evaluation again.
 * A bit hacky at the moment, but useful for quickly running tests
 */
public class EvalCache {
    HashMap<EvalCacheKey, EvalCacheValue> cachedEvals;

    public EvalCache() {
        cachedEvals = new HashMap<EvalCacheKey, EvalCacheValue>();
    }

    public void insert(EvalCacheKey key, EvalCacheValue val) {
        cachedEvals.put(key, val);
    }

    public EvalCacheValue getCachedEvaluation(ProblemDefinition problemDef, ControlProviderDefinition controlDef) {
        EvalCacheKey key = new EvalCacheKey(problemDef, controlDef);
        EvalCacheValue value = cachedEvals.get(key);
        return value;
    }
}
