package edu.cmu.cs.graphics.hopper.oracle;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;

/** Capable of providing precise control sequences which solve a given problem.
 * This could draw from a predefined list of answers, it could forward a challenge to a user, or something else entirely... */
public abstract class ChallengeOracle<C extends Control> {

    /**Given a particular problem, returns a control sequence which is guaranteed to solve that problem, or null if the oracle
     * is unable to provide a solution to the problem (egad!) */
    public abstract ControlProvider<C> solveChallenge(ProblemDefinition problemDef, AvatarDefinition avatarDef);

    /** Sends a problem instance that was already simulated (usually with sampling)
     * to this oracle so it may "review" it (ie: let the user view it)
     * Just a bit of a helper for debug viualization. */
    public void sendForReview(ProblemInstance problem) {}
}
