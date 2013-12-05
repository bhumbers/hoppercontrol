package edu.cmu.cs.graphics.hopper.oracle;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.problems.Problem;

import java.util.Map;

/** An oracle which has a predefined association between problems & solutions. Returns null solutions if not present in associative set.
 * Useful for testing by preloading the problem-solution set.*/
public class AssociativeOracle<C extends Control> extends ChallengeOracle<C> {
    //Keys are problem hashes, values are solutions for associated problem
    Map<String, ControlProvider> solutionMap;

    public AssociativeOracle() {
        //TODO: Init gui? Would be efficient to only do it once rather than every time a challenge is given
    }

    @Override
    public ControlProvider<C> solveChallenge(Problem p) {
        //TODO: Send problem to GUI, wait for user to complete, return provided control

        return new ControlProvider<C>();
    }
}
