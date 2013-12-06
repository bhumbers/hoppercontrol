package edu.cmu.cs.graphics.hopper.eval;

/** An instantiable control evaluator */
public abstract class EvaluatorDefinition {
    /** Generates evaluator for this definition */
    public abstract Evaluator create();
}
