package edu.cmu.cs.graphics.hopper.explore;

import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.io.IOUtils;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import net.sf.javaml.core.kdtree.KDTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** An explorer which intelligently selects which control sequence to test next */
public class SmartControlExplorer<C extends Control> extends Explorer<C> {
    //Maps from problems to known solution sequences (ControlProvider)
    KDTree controlsByProblem;
    int numControlsByProblem;

    //Ordered list of controls to attempt for current problem
    List<ControlProviderDefinition<C>> sequencesToTryForProblem;
    int nextControlSequenceIdx;

    @Override
    public void loadEnsemble(String inputEnsemblePath) {
        //For now: load sol files, add corresponding controls to ensemble
        List<ProblemSolutionEntry> entries = IOUtils.instance().loadAllProblemSolutionEntriesInDir(inputEnsemblePath);
        for (ProblemSolutionEntry entry : entries)
            addToControlEnsemble(entry.problem, entry.solution);
    }

    @Override
    public void initExploration() {
    }

    @Override
    protected void prepareForProblem(ProblemDefinition problemDef) {
        if (sequencesToTryForProblem == null)
            sequencesToTryForProblem = new ArrayList<ControlProviderDefinition<C>>();

        nextControlSequenceIdx = 0;
        sequencesToTryForProblem.clear();

        //Generate order to test controls based on similarity to given problem
        double[] problemParams = problemDef.getParamsArray();
        final int MAX_NEAREST_TO_RETURN = 50;
        int nearestK = Math.min(numControlsByProblem, MAX_NEAREST_TO_RETURN); //KD tree seems to explode if this is too high (null pointer exception)
        if (controlsByProblem != null) {
            Object[] orderedControls = controlsByProblem.nearest(problemParams, nearestK);
            for (Object orderedControl : orderedControls)
                sequencesToTryForProblem.add((ControlProviderDefinition<C>)orderedControl);
        }
    }

    @Override
    protected ProblemDefinition getNextProblemToTest() {
        if (unsolvedProblems.size() > 0)
            return unsolvedProblems.iterator().next();
        return null;
    }

    @Override
    protected ControlProviderDefinition<C> getNextControlSequence(ProblemDefinition p) {
        //Return next item in list
        ControlProviderDefinition<C> provider = null;
        if (nextControlSequenceIdx <= sequencesToTryForProblem.size() - 1) {
            provider = sequencesToTryForProblem.get(nextControlSequenceIdx);
            nextControlSequenceIdx++;
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
    protected void addToControlEnsemble(ProblemDefinition problem, ControlProviderDefinition<C> control) {
        double[] problemParams = problem.getParamsArray();
        if (controlsByProblem == null) {
            int k = problemParams.length;
            controlsByProblem = new KDTree(k);
            numControlsByProblem = 0;
        }

        //Add the solution to ensemble, indexed by problem
        controlsByProblem.insert(problemParams, control);
        numControlsByProblem++;
    }
}
