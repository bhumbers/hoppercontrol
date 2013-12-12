package edu.cmu.cs.graphics.hopper.explore;

public final class ExplorerLogEntry {
    public final int numUnsolvedProblems;
    public final int numSolvedProblems;
    public final int numFailedProblems;

    public final int numTests;
    public final int numChallenges;

    public ExplorerLogEntry(int numUnsolvedProblems, int numSolvedProblems, int numFailedProblems,
                 int numTests, int numChallenges) {
        this.numUnsolvedProblems = numUnsolvedProblems;
        this.numSolvedProblems = numSolvedProblems;
        this.numFailedProblems = numFailedProblems;
        this.numTests = numTests;
        this.numChallenges = numChallenges;
    }

    protected String getCSVRow() {
        return String.format("%d, %d, %d, %d, %d,\n", numUnsolvedProblems, numSolvedProblems, numFailedProblems, numTests, numChallenges);
    }
}