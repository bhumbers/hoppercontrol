package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.oracle.ChallengeOracle;
import edu.cmu.cs.graphics.hopper.problems.Problem;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Runs automated explorations, given a problem set*/
public abstract class Explorer<C extends Control> {
    protected final class SolvedProblemEntry {
        final Problem problem;              //problem that was solved
        final ControlProvider solution;  //solution sequence used to solve it

        SolvedProblemEntry(Problem problem, ControlProvider solution) {
            this.problem = problem;
            this.solution = solution;
        }
    }


    protected int numTests;
    protected int numOracleChallenges;

    ChallengeOracle<C> oracle;

    Set<Problem> unsolvedProblems;
    Set<SolvedProblemEntry> solvedProblems;
    Set<Problem> oracleChallengeProblems;

    /** Runs exploration in a continuous loop until all problems are solved */
    public void explore(List<Problem> problems, ChallengeOracle<C> oracle) {explore(problems, oracle, -1);}

    /** Runs exploration in a continuous loop until max control tests is reached or all problems are solved
     * Runs until completion if maxTests == -1 (or anything < 0). */
    public void explore(List<Problem> problems, ChallengeOracle<C> oracle, int maxTests) {
        numTests = 0;
        numOracleChallenges = 0;
        this.oracle = oracle;
        unsolvedProblems = new HashSet<Problem>();
        solvedProblems = new HashSet<SolvedProblemEntry>();
        oracleChallengeProblems = new HashSet<Problem>();

        unsolvedProblems.addAll(problems);

        initExploration();

        //While there remain problems to solve, get a new one and try to solve it
        while (!unsolvedProblems.isEmpty() && (maxTests < 0  || numTests < maxTests)) {
            Problem p = getNextProblemToTest();
            prepareForNextProblem();

            //Test control sequences until problem is solved or we give up
            boolean problemSolved = false;
            ControlProvider<C> potentialSolution = getNextControlSequence(p);
            while (potentialSolution != null) {
                problemSolved = p.runControlTest(potentialSolution);
                numTests++;
                if (problemSolved)
                    break;
            }

            //If solved, mark it as such
            //Otherwise, add to list of problems for oracle to solve
            if (problemSolved)
                markProblemSolved(p, potentialSolution);
            else {
                oracleChallengeProblems.add(p);
                //TODO: should we also remove from unsolvedProblems so that we don't try to re-solve while waiting for oracle?
            }

            //If this explorer wishes to do so at this moment, poll the oracle
            Problem challenge = getNextChallengeProblem();
            if (challenge != null) {
                ControlProvider<C> challengeSolution = oracle.solveChallenge(challenge);
                markProblemSolved(challenge, challengeSolution);
            }
        }
    }

    protected void markProblemSolved(Problem problem, ControlProvider<C> solution) {
        unsolvedProblems.remove(problem);
        solvedProblems.add(new SolvedProblemEntry(problem, solution));
    }

    /**Sets up for a new exploration (called at start of explore())  */
    protected abstract void initExploration();

    /** Change any internal state in preparation for solving a new problem */
    protected abstract void prepareForNextProblem();

    /** Returns what this explorer believes is the most useful problem to try and solve */
    protected abstract Problem getNextProblemToTest();

    /** Returns what this explorer believes is the most useful control provider (ie: sequence) to test next on given problem,
     * or null if the explorer wishes to give up on the problem and hand it to the oracle for solution. */
    protected abstract ControlProvider<C> getNextControlSequence(Problem p);

    /** Returns the next most useful problem to send to user/oracle challenge for this explorer, or null
     * if this explorer currently does not wish to send a problem to the oracle */
    protected abstract Problem getNextChallengeProblem();
}
