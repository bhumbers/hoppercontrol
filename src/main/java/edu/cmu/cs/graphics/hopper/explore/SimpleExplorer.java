package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.problems.Problem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** A baseline explorer that doesn't do anything smart in terms of prioritizing
 * which examples to send to the user or which controls to test at each timestep. */
public class SimpleExplorer<C extends Control> extends Explorer<C> {
    List<Problem> unsolvedProblemList;

    //Control providers/sequences usable by this explorer
    List<ControlProvider<C>> controlProviders;
    int nextControlProviderIdx;

    @Override
    public void initExploration() {
        controlProviders = new ArrayList<ControlProvider<C>>();

        //TESTING: Create a dummy controller to use
        controlProviders.add(new ControlProvider<C>());

        unsolvedProblemList = new ArrayList<Problem>();
        unsolvedProblemList.addAll(unsolvedProblems);
    }

    @Override
    protected void prepareForNextProblem() {
        nextControlProviderIdx = 0;
    }

    @Override
    protected Problem getNextProblemToTest() {
        return unsolvedProblemList.get(0);
    }

    @Override
    protected ControlProvider<C> getNextControlSequence(Problem p) {
        //Just return next sequence in the list
        ControlProvider<C> provider = controlProviders.get(nextControlProviderIdx);
        if (nextControlProviderIdx < controlProviders.size() - 1)
            nextControlProviderIdx++;
        return provider;
    }

    @Override
    protected Problem getNextChallengeProblem() {
        //Return something arbitrary from the map of marked oracle problems
        if (!oracleChallengeProblems.isEmpty())
            return oracleChallengeProblems.iterator().next();
        return null;
    }
}
