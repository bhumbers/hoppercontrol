package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.oracle.ChallengeOracle;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Runs automated explorations, given a problem set*/
public abstract class Explorer<C extends Control> {
    private static final Logger log = LoggerFactory.getLogger(Explorer.class);

    protected final class SolvedProblemEntry {
        final ProblemDefinition problem;              //problem that was solved
        final ControlProvider solution;  //solution sequence used to solve it

        SolvedProblemEntry(ProblemDefinition problem, ControlProvider solution) {
            this.problem = problem;
            this.solution = solution;
        }
    }


    protected int numTests;
    protected int numOracleChallenges;

    public int getNumTests() {return numTests;}
    public int getNumOracleChallenges() {return numOracleChallenges;}

    public int getNumProblems() {return getNumSolvedProblems() + getNumUnsolvedProblems();}
    public int getNumUnsolvedProblems() {return unsolvedProblems.size();}
    public int getNumSolvedProblems() {return solvedProblems.size();}

    ChallengeOracle<C> oracle;

    AvatarDefinition avatarDef;

    Set<ProblemDefinition> unsolvedProblems;
    Set<SolvedProblemEntry> solvedProblems;
    Set<ProblemDefinition> oracleChallengeProblems;

    /** Runs exploration in a continuous loop until all problems are solved */
    public void explore(List<ProblemDefinition> problems, AvatarDefinition avatarDef, ChallengeOracle<C> oracle) {explore(problems, avatarDef, oracle, -1);}

    /** Runs exploration in a continuous loop until max control tests is reached or all problems are solved
     * Runs until completion if maxTests == -1 (or anything < 0). */
    public void explore(List<ProblemDefinition> problems, AvatarDefinition avatarDef, ChallengeOracle<C> oracle, int maxTests) {
        numTests = 0;
        numOracleChallenges = 0;
        this.oracle = oracle;
        this.avatarDef = avatarDef;

        //Note: Linked hash sets used to preserve insertion ordering for iteration
        unsolvedProblems = new LinkedHashSet<ProblemDefinition>();
        solvedProblems = new LinkedHashSet<SolvedProblemEntry>();
        oracleChallengeProblems = new LinkedHashSet<ProblemDefinition>();

        unsolvedProblems.addAll(problems);

        initExploration();

        //While there remain problems to solve, get a new one and try to solve it
        while (!unsolvedProblems.isEmpty() && (maxTests < 0  || numTests < maxTests)) {
            ProblemDefinition problemDef = getNextProblemToTest();
            prepareForNextProblem();

            log.info("Attempting to solve problem: " + problemDef.toString());

            //Test control sequences until problem is solved or we give up
            boolean problemSolved = false;
            ControlProvider<C> potentialSolution = getNextControlSequence(problemDef);
            while (potentialSolution != null) {
                ProblemInstance problem = new ProblemInstance(problemDef, avatarDef, potentialSolution);
                problem.init();
                problem.run();
                numTests++;
                problemSolved = (problem.getStatus() == ProblemInstance.ProblemStatus.SOLVED);
                if (problemSolved)
                    break;
                else
                    potentialSolution = getNextControlSequence(problemDef);
            }

            //If solved, mark it as such
            //Otherwise, add to list of problems for oracle to solve
            if (problemSolved)    {
                log.info("Found solution to problem: " + problemDef.toString());
                markProblemSolved(problemDef, potentialSolution);
            }
            else {
                log.info("Unable to find solution to problem; adding to oracle challenge list: " + problemDef.toString());
                oracleChallengeProblems.add(problemDef);
                //TODO: should we also remove from unsolvedProblems so that we don't try to re-solve while waiting for oracle?
            }

            //If this explorer wishes to do so at this moment, poll the oracle
            ProblemDefinition challenge = getNextChallengeProblem();
            if (challenge != null) {
                log.info("Sending challenge #" + numOracleChallenges + " to oracle: " + problemDef.toString());
                numOracleChallenges++;
                ControlProvider<C> challengeSolution = oracle.solveChallenge(challenge, avatarDef);

                //DEBUGGING: Verify that oracle solution is correct.
                ProblemInstance problem = new ProblemInstance(problemDef, avatarDef, challengeSolution);
                problem.init();
                problem.run();
                if (problem.getStatus() != ProblemInstance.ProblemStatus.SOLVED) {
                    log.warn("Oracle returned an incorrect challenge solution. That shouldn't happen. ProblemDefinition hash: " + challenge.hashCode());
                }
                else {
                    log.info("Oracle successfully solved challenge; adding to list of solved problems: " + problemDef.toString());
                    markProblemSolved(challenge, challengeSolution);
                    onChallengeSolutionGiven(challengeSolution);
                }
            }
        }
    }

    protected void markProblemSolved(ProblemDefinition problem, ControlProvider<C> solution) {
        unsolvedProblems.remove(problem);
        oracleChallengeProblems.remove(problem);   //remove in case it's marked as oracle challenge
        solvedProblems.add(new SolvedProblemEntry(problem, solution));
    }

    /**Sets up for a new exploration (called at start of explore())  */
    protected abstract void initExploration();

    /** Change any internal state in preparation for solving a new problem */
    protected abstract void prepareForNextProblem();

    /** Returns what this explorer believes is the most useful problem to try and solve */
    protected abstract ProblemDefinition getNextProblemToTest();

    /** Returns what this explorer believes is the most useful control provider (ie: sequence) to test next on given problem,
     * or null if the explorer wishes to give up on the problem and hand it to the oracle for solution. */
    protected abstract ControlProvider<C> getNextControlSequence(ProblemDefinition p);

    /** Returns the next most useful problem to send to user/oracle challenge for this explorer, or null
     * if this explorer currently does not wish to send a problem to the oracle */
    protected abstract ProblemDefinition getNextChallengeProblem();

    /** Runs any additional logic (aside from marking a problem solved) for when a new challenge solution is provided */
    protected void onChallengeSolutionGiven(ControlProvider<C> challengeSolution) {}
}
