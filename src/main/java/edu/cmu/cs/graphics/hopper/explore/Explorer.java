package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.eval.*;
import edu.cmu.cs.graphics.hopper.io.IOUtils;
import edu.cmu.cs.graphics.hopper.oracle.ChallengeOracle;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/** Runs automated explorations, given a problem set*/
public abstract class Explorer<C extends Control> {

    private static final Logger log = LoggerFactory.getLogger(Explorer.class);

    protected ExplorerLog expLog;
    protected EvalCache evalCache = null;

    protected int numTests;
    protected int numOracleChallenges;

    String explorationName;

    //Oracles consulted for challenge problems. Challenges are presented to oracles in successive
    //order of this list until some Oracle solves it.
    List<ChallengeOracle<C>> oracles;

    AvatarDefinition avatarDef;
    EvaluatorDefinition evalDef;

    Set<ProblemDefinition> unsolvedProblems;
    Set<ProblemSolutionEntry> solvedProblems;
    Set<ProblemDefinition> failedProblems;      //problems submitted to oracles for sol, but for which all oracles failed to find sol
    Set<ProblemDefinition> oracleChallengeProblems;

    boolean solsSaved = false;
    String solsSavePath = "";

    boolean logSaved = false;
    String logSavePath = "";

    boolean evalsSaved = false;
    String evalsSavePath = "";

    //If true, solutions from oracle are verified for correctness; if incorrect, sampled simulation is forwarded to the oracle for review
    boolean verifyOracleSols = false;

    //Max control tests run on a problem before we give up and hand it to the oracles (arbitrarily high if == -1)
    int maxTestsPerProblem = -1;

    FileWriter logWriter;

    public ExplorerLog getLog() {return expLog;}

    public int getNumTests() {return numTests;}
    public int getNumOracleChallenges() {return numOracleChallenges;}

    public int getNumProblems() {return getNumSolvedProblems() + getNumUnsolvedProblems();}
    public int getNumUnsolvedProblems() {return unsolvedProblems.size();}
    public int getNumSolvedProblems() {return solvedProblems.size();}
    public int getNumFailedProblems() {return failedProblems.size();}

    public Collection<ProblemSolutionEntry> getSolvedProblems() {return solvedProblems;}

    public void setName(String val) {explorationName = val;}

    public void setSolutionsSaved(boolean val) { solsSaved = val; }
    public void setSolutionsSavePath(String path) { solsSavePath = path; }
    public void setLogSaved(boolean val) { logSaved = val; }
    public void setLogSavePath(String path) { logSavePath = path; }
    public void setEvalsSaved(boolean val) {evalsSaved = val;}
    public void setEvalsSavePath(String path) { evalsSavePath = path; }

    public void setMaxTestsPerProblem(int val) {maxTestsPerProblem = val;}

    public void setVerifyOracleSols(boolean val) { verifyOracleSols = val;}

    public void setEvalCache(EvalCache val) {evalCache = val;}

    /** Runs exploration in a continuous loop until all problems are solved */
    public void explore(List<ProblemDefinition> problems, AvatarDefinition avatarDef, EvaluatorDefinition evalDef, List<ChallengeOracle<C>> oracles) {
        explore(problems, avatarDef, evalDef, oracles, -1);
    }

    /** Runs exploration in a continuous loop until max control tests is reached or all problems are solved
     * Runs until completion if maxTests == -1 (or anything < 0). */
    public void explore(List<ProblemDefinition> problems, AvatarDefinition avatarDef, EvaluatorDefinition evalDef, List<ChallengeOracle<C>> oracles, int maxTests) {
        expLog = new ExplorerLog();
        if (logSaved) {
            IOUtils.instance().ensurePathExists(logSavePath);
            try {
                logWriter = new FileWriter(logSavePath + explorationName + "_ExpLog.csv");
                logWriter.write(expLog.getCSVHeader());
                logWriter.flush();
            }
            catch (IOException error) {
                log.error("Error creating file path: " + logSavePath);
            }
        }

        if (evalsSaved)
            IOUtils.instance().ensurePathExists(evalsSavePath);

        numTests = 0;
        numOracleChallenges = 0;

        this.oracles = new ArrayList<ChallengeOracle<C>>();
        this.oracles.addAll(oracles);

        this.avatarDef = avatarDef;
        this.evalDef = evalDef;

        //Note: Linked hash sets used to preserve insertion ordering for iteration
        unsolvedProblems = new LinkedHashSet<ProblemDefinition>();
        solvedProblems = new LinkedHashSet<ProblemSolutionEntry>();
        failedProblems = new LinkedHashSet<ProblemDefinition>();
        oracleChallengeProblems = new LinkedHashSet<ProblemDefinition>();

        unsolvedProblems.addAll(problems);

        initExploration();

        //While there remain problems to solve, get a new one and try to solve it
        int problemIdx = 0;
        while (!unsolvedProblems.isEmpty() && (maxTests < 0  || numTests < maxTests)) {
            ProblemDefinition problemDef = getNextProblemToTest();
            prepareForProblem(problemDef);

            log.info("Attempting to solve problem #" + problemIdx);

            int numTestsRunForProblem = 0;

            //Test control sequences until problem is solved or we give up
            boolean problemSolved = false;
            ControlProviderDefinition<C> potentialSolution = getNextControlSequence(problemDef);
            while (potentialSolution != null && (maxTestsPerProblem < 0 || numTestsRunForProblem < maxTestsPerProblem)) {
                //If configured to do so, try to use a cached eval first
                Evaluator.Status evalResult = Evaluator.Status.RUNNING;
                boolean foundCachedEval = false;
                if (evalCache != null) {
                    EvalCacheValue evalValue = evalCache.getCachedEvaluation(problemDef, potentialSolution);
                    if (evalValue != null) {
                        evalResult = evalValue.status;
                        foundCachedEval = true;
                    }
                }

                //Otherwise, or if no cached sol found, do a true sim eval on the problem
                if (!foundCachedEval) {
//                    long t0 = System.currentTimeMillis();
                    ProblemInstance problem = new ProblemInstance(problemDef, avatarDef, evalDef, potentialSolution);
                    problem.init();
                    problem.run();
                    evalResult = problem.getStatus();
//                    long t1 = System.currentTimeMillis();
//                    log.info("EVAL RUN TEST: " + (t1 - t0));
                }

                //If configured to do so, save the evaluation result
                if (evalsSaved) {
                    int evalNum = numTests;
                    EvalCacheKey key = new EvalCacheKey(problemDef, potentialSolution);
                    int evalKeyHash = key.hashCode();
                    String filename = String.format("%h", evalKeyHash) + ".ech";
                    log.info("Saving eval to disk: " + filename);
                    IOUtils.instance().saveEvalCacheEntry(
                            new EvalCacheEntry(new EvalCacheKey(problemDef, potentialSolution),new EvalCacheValue(evalResult)),
                            evalsSavePath, filename);
                }

                numTests++; numTestsRunForProblem++;

                problemSolved = (evalResult == Evaluator.Status.SUCCESS);
                if (problemSolved)
                    break;
                else
                    potentialSolution = getNextControlSequence(problemDef);
            }

            //If solved, mark it as such
            //Otherwise, add to list of problems for oracle to solve
            if (problemSolved)    {
                log.info("Found solution to problem #" + problemIdx + " after " + numTestsRunForProblem + " test(s) run");
                markProblemSolved(problemDef, potentialSolution);
            }
            else {
                log.info("No solution to problem; adding problem #" + problemIdx + " to oracle challenge list after " + numTestsRunForProblem + " test(s) run");
                unsolvedProblems.remove(problemDef);  //remove from unsolved set... oracle will solve for us (or fail trying)
                oracleChallengeProblems.add(problemDef);
                addLogEntry();
            }
            problemIdx++;

            //If this explorer wishes to do so at this moment, poll the oracles
            ProblemDefinition challenge = getNextChallengeProblem();
            if (challenge != null)
                sendChallengeToOracles(challenge);
        }

        if (logWriter != null)   {
            try {
                logWriter.close();
            }
            catch (IOException error) {
                log.error("Error closing log file: " + logSavePath);
            }
        }
    }

    protected void sendChallengeToOracles(ProblemDefinition challenge) {
        int oracleChallengeIdx = numOracleChallenges;
        log.info("Sending challenge #" + oracleChallengeIdx + " to oracles");

        boolean challengeSolFound = false;
        for (int oracleIdx = 0; oracleIdx < oracles.size(); oracleIdx++) {
            ChallengeOracle<C> oracle = oracles.get(oracleIdx);
            ControlProviderDefinition<C> challengeSolution = oracle.solveChallenge(challenge, avatarDef, evalDef);

            boolean oracleSolutionOk = true;

            if (challengeSolution == null) {
                log.info("Oracle #" + oracleIdx + " returned null solution for challenge # " + oracleChallengeIdx);
                oracleSolutionOk = false;
            }
            //Verify that oracle solution is correct if requested to do so
            else if (verifyOracleSols) {
                ProblemInstance problem = new ProblemInstance(challenge, avatarDef, evalDef, challengeSolution);
                problem.setUseSampling(true); //for debugging
                problem.init();
                problem.run();
                if (problem.getStatus() != Evaluator.Status.SUCCESS) {
                    log.info("Oracle #" + oracleIdx + " returned an incorrect solution to challenge # " + oracleChallengeIdx);
                    oracleSolutionOk = false;
//                    oracle.sendForReview(problem);
                }
            }

            if (oracleSolutionOk)         {
                log.info("Oracle #" + oracleIdx + " successfully solved challenge #" + numOracleChallenges + " ; marking as solved");
                challengeSolFound = true;
                markProblemSolved(challenge, challengeSolution);
                onChallengeSolutionGiven(challenge, challengeSolution);
                break;
            }
        }

        //Mark failed if no oracle found a solution at this point
        if (challengeSolFound == false) {
            markProblemFailed(challenge);
        }

        numOracleChallenges++;
    }

    private void markProblemSolved(ProblemDefinition problem, ControlProviderDefinition<C> solution) {
        //NOTE: If we're marking it solved, it will currently be either in either unsolved set or oracle challenge set
        //For simplicity, just be sure it's removed from both
        unsolvedProblems.remove(problem);
        oracleChallengeProblems.remove(problem);
        solvedProblems.add(new ProblemSolutionEntry(problem, solution));

        if (solsSaved) {
            int solNum = solvedProblems.size();
            String filename = String.format("%05d", solNum) + ".sol";
            log.info("Saving solution to disk: " + filename);
            IOUtils.instance().saveProblemSolutionEntry(new ProblemSolutionEntry(problem, solution), solsSavePath, filename);
        }

        addLogEntry();
    }

    /** Marks problem as having no solution to be found (even from oracles) */
    protected void markProblemFailed(ProblemDefinition problem) {
        oracleChallengeProblems.remove(problem);
        failedProblems.add(problem);

        addLogEntry();
    }

    protected void addLogEntry() {
        ExplorerLogEntry entry = new ExplorerLogEntry(
                getNumTests(),
                getNumOracleChallenges(),
                getNumUnsolvedProblems(),
                getNumSolvedProblems(),
                getNumFailedProblems()
                );
        expLog.entries.add(entry);

        //Write the new log CSV line if being saved
        if (logSaved && logWriter != null) {
            try {
                logWriter.write(entry.getCSVRow());
                logWriter.flush();
            }
            catch (IOException error) {
                log.error("Error writing to log file: " + logSavePath);
            }
        }
    }

    /**Sets up for a new exploration (called at start of explore())  */
    protected abstract void initExploration();

    /** Change any internal state in preparation for solving a new problem */
    protected abstract void prepareForProblem(ProblemDefinition problemDef);

    /** Returns what this explorer believes is the most useful problem to try and solve */
    protected abstract ProblemDefinition getNextProblemToTest();

    /** Returns what this explorer believes is the most useful control provider (ie: sequence) to test next on given problem,
     * or null if the explorer wishes to give up on the problem and hand it to the oracle for solution. */
    protected abstract ControlProviderDefinition<C> getNextControlSequence(ProblemDefinition p);

    /** Returns the next most useful problem to send to user/oracle challenge for this explorer, or null
     * if this explorer currently does not wish to send a problem to the oracle */
    protected abstract ProblemDefinition getNextChallengeProblem();

    /** Runs any additional logic (aside from marking a problem solved) for when a new challenge solution is provided */
    protected void onChallengeSolutionGiven(ProblemDefinition challenge, ControlProviderDefinition<C> challengeSolution) {}
}
