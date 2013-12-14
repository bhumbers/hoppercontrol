package edu.cmu.cs.graphics.hopper.eval;

import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

public final class EvalCacheKey {
    final ProblemDefinition problemDef;
    final ControlProviderDefinition controlDef;

    public EvalCacheKey(ProblemDefinition problemDef, ControlProviderDefinition controlDef) {
        this.problemDef = problemDef;
        this.controlDef = controlDef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EvalCacheKey that = (EvalCacheKey) o;

        if (!controlDef.equals(that.controlDef)) return false;
        if (!problemDef.equals(that.problemDef)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = problemDef.hashCode();
        result = 31 * result + controlDef.hashCode();
        return result;
    }
}
