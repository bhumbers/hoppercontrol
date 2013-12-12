package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;

import java.util.ArrayList;
import java.util.List;

/** A baseline explorer that doesn't do anything smart in terms of prioritizing
 * which examples to send to the user or which controls to test at each timestep. */
public class SimpleExplorer<C extends Control> extends Explorer<C> {
    //Control providers/sequences usable by this explorer
    List<ControlProvider<C>> controlEnsemble;
    int nextControlProviderIdx;

    @Override
    public void initExploration() {
        controlEnsemble = new ArrayList<ControlProvider<C>>();
    }

    @Override
    protected void prepareForProblem(ProblemDefinition problemDef) {
        nextControlProviderIdx = 0;
    }

    @Override
    protected ProblemDefinition getNextProblemToTest() {
        if (unsolvedProblems.size() > 0)
            return unsolvedProblems.iterator().next();
        return null;
    }

    @Override
    protected ControlProvider<C> getNextControlSequence(ProblemDefinition p) {
        //Just return next sequence in the list, if available
        ControlProvider<C> provider = null;
        if (nextControlProviderIdx <= controlEnsemble.size() - 1) {
            provider = controlEnsemble.get(nextControlProviderIdx);
            nextControlProviderIdx++;
        }
        return provider;
    }

    @Override
    protected ProblemDefinition getNextChallengeProblem() {
        //Return something arbitrary from the map of marked oracle problems
        if (!oracleChallengeProblems.isEmpty())
            return oracleChallengeProblems.iterator().next();
        return null;
    }

    @Override
    protected void onChallengeSolutionGiven(ProblemDefinition challenge, ControlProvider<C> challengeSolution) {
        //Add the new solution to our ensemble (control vocabulary)
        controlEnsemble.add(challengeSolution);
    }
}
