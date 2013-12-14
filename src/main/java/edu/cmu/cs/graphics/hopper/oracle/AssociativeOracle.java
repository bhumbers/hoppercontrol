package edu.cmu.cs.graphics.hopper.oracle;

import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.eval.EvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.explore.ProblemSolutionEntry;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

import net.sf.javaml.core.kdtree.KDTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** An oracle which has a predefined association between problems & solutions. Returns null solutions if not present in associative set.
 * Useful for testing by preloading the problem-solution set.*/
public class AssociativeOracle<C extends Control> extends ChallengeOracle<C> {
    private static final Logger log = LoggerFactory.getLogger(AssociativeOracle.class);

    //Keys are problem params put in array, vals are ControlProvider solutions for associated problem
    KDTree solutionsByProblem;
    int k; //size of k for kd tree

    //The full list of problem-solution entries for this map (also in KD tree, but list is useful for debug viewing)
    List<ProblemSolutionEntry> solutionsList;

    public AssociativeOracle() {
        solutionsByProblem = null; //wait until first entry is added to determine k for tree
        solutionsList = new ArrayList<ProblemSolutionEntry>();
        k = -1;
    }

    public void addSolutionEntry(ProblemDefinition problem, ControlProviderDefinition<C> solution) {
        double[] problemParams = problem.getParamsArray();
        if (k >= 0 && problemParams.length != k)  {
           log.error("Failed to add solution entry to oracle: Oracle accepts problem params of size " + k +
                        ", but given problem was of size " + problemParams.length);
            return;
        }

        //Create tree if this is the first problem being added
        if (k == -1) {
            k = problemParams.length;
            solutionsByProblem = new KDTree(k);
        }

        solutionsByProblem.insert(problemParams, solution);
        solutionsList.add(new ProblemSolutionEntry(problem, solution));
    }

    @Override
    public ControlProviderDefinition<C> solveChallenge(ProblemDefinition problemDef, AvatarDefinition avatarDef,
                                                       EvaluatorDefinition evalDef, ControlProviderDefinition suggestedControl) {

        //Note: doesn't listen to suggested control; just uses map

        if (solutionsByProblem != null) {
            double[] problemParams = problemDef.getParamsArray();
                if (k >= 0 && problemParams.length != k)  {
                log.error("Bad problem given to oracle for solving, returning null. Oracle accepts problem params of size " + k +
                        ", but given problem was of size " + problemParams.length);
                return null;
            }

            ControlProviderDefinition<C> solution = (ControlProviderDefinition<C>)solutionsByProblem.search(problemParams);
            return solution;
        }
        return null;
    }
}
