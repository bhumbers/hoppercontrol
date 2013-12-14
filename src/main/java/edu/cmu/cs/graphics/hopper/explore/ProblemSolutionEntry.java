package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

/**A problem with associated solution sequence */
public final class ProblemSolutionEntry {
    final ProblemDefinition problem;              //problem that was solved
    final ControlProviderDefinition solution;  //solution sequence used to solve it

    public ProblemSolutionEntry(ProblemDefinition problem, ControlProviderDefinition solution) {
        this.problem = problem;
        this.solution = solution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProblemSolutionEntry that = (ProblemSolutionEntry) o;

        if (!problem.equals(that.problem)) return false;
        if (!solution.equals(that.solution)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = problem.hashCode();
        result = 31 * result + solution.hashCode();
        return result;
    }
}
