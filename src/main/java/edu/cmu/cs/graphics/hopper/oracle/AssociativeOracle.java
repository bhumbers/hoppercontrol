package edu.cmu.cs.graphics.hopper.oracle;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.eval.EvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

import java.util.HashMap;
import java.util.Map;

/** An oracle which has a predefined association between problems & solutions. Returns null solutions if not present in associative set.
 * Useful for testing by preloading the problem-solution set.*/
public class AssociativeOracle<C extends Control> extends ChallengeOracle<C> {
    //Keys are problem hashes, values are solutions for associated problem
    Map<ProblemDefinition, ControlProvider<C>> solutionMap;

    public AssociativeOracle() {
        solutionMap = new HashMap<ProblemDefinition, ControlProvider<C>>();

        //TODO: provide a way to add items to solution map
    }

    @Override
    public ControlProvider<C> solveChallenge(ProblemDefinition problemDef, AvatarDefinition avatarDef, EvaluatorDefinition evalDef) {
        //TODO: Return from solution map
        return solutionMap.get(problemDef);
    }
}
