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
}
