package edu.cmu.cs.graphics.hopper.oracle;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.problems.Problem;

/** An oracle which sends a problem to user interface for solution. Calls to this oracle may block for awhile... */
public class UserOracle<C extends Control> extends ChallengeOracle<C>{

    public UserOracle() {
        //TODO: Init gui? Would be efficient to only do it once rather than every time a challenge is given
    }

    @Override
    public ControlProvider<C> solveChallenge(Problem p) {
        //TODO: Send problem to GUI, wait for user to complete, return provided control

        return new ControlProvider<C>();
    }
}
