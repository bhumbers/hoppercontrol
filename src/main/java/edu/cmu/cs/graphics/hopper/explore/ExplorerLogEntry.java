package edu.cmu.cs.graphics.hopper.explore;

public final class ExplorerLogEntry {
    public final int numTests;
    public final int numChallenges;

    public final int numUnsolvedProblems;
    public final int numSolvedProblems;
    public final int numFailedProblems;

    public ExplorerLogEntry(int numTests, int numChallenges,
                            int numUnsolvedProblems, int numSolvedProblems, int numFailedProblems)
    {
        this.numTests = numTests;
        this.numChallenges = numChallenges;

        this.numUnsolvedProblems = numUnsolvedProblems;
        this.numSolvedProblems = numSolvedProblems;
        this.numFailedProblems = numFailedProblems;
    }

    protected String getCSVRow() {
        return String.format("%d, %d, %d, %d, %d,\n", numTests, numChallenges, numUnsolvedProblems, numSolvedProblems, numFailedProblems);
    }
}