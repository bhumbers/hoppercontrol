package edu.cmu.cs.graphics.hopper.oracle;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

/** Capable of providing precise control sequences which solve a given problem.
 * This could draw from a predefined list of answers, it could forward a challenge to a user, or something else entirely... */
public abstract class ChallengeOracle<C extends Control> {

    /**Given a particular problem, returns a control sequence which is guaranteed to solve that problem, or null if the oracle
     * is unable to provide a solution to the problem (egad!) */
    public abstract ControlProvider<C> solveChallenge(ProblemDefinition problemDef, AvatarDefinition avatarDef);
}
