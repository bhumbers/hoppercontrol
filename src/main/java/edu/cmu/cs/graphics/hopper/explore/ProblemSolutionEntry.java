package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

/**A problem with associated solution sequence */
public final class ProblemSolutionEntry {
    final ProblemDefinition problem;              //problem that was solved
    final ControlProvider solution;  //solution sequence used to solve it

    public ProblemSolutionEntry(ProblemDefinition problem, ControlProvider solution) {
        this.problem = problem;
        this.solution = solution;
    }
}
