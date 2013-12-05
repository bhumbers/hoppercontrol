package edu.cmu.cs.graphics.hopper.oracle;

import edu.cmu.cs.graphics.hopper.TestbedFrame;
import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.Control;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests.ProblemInstanceTest;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.UIManager;
import javax.swing.JFrame;

/** An oracle which sends a problem to user interface for solution. Calls to this oracle may block for awhile... */
public class UserOracle<C extends Control> extends ChallengeOracle<C>{
    private static final Logger log = LoggerFactory.getLogger(UserOracle.class);

    /** The problem "runner" */
    ProblemInstanceTest test;

        public UserOracle() {
            //Init the GUI window
            initGUI();
        }

    protected void initGUI() {
        //TODO: This is specific to biped hopper tests for now... make generic? -bh, 12.5.2013
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            log.warn("Could not set the look and feel to nimbus.  ");
        }
        TestbedModel model = new TestbedModel();

        //Not fun, but the hopper avatar requires a *lot* of iterations/short timestep to converge
        //Hoping this can be reduced by tweaking how we use joints, but not sure...
        model.getSettings().getSetting(TestbedSettings.PositionIterations).value = 30;
        model.getSettings().getSetting(TestbedSettings.VelocityIterations).value = 50;

        TestbedPanel panel = new TestPanelJ2D(model);

        test = new ProblemInstanceTest();
        model.addTest(test);

        TestbedFrame testbed = new TestbedFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        testbed.setSize(1200, 700);
        testbed.setVisible(true);

        //Biped hopper is only stable at short timesteps
        int targetFps = 50;
        int targetHz =  targetFps * 20;
        testbed.controller.setFrameRate(targetFps);
        testbed.model.getSettings().getSetting(TestbedSettings.Hz).value = targetHz;

        testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public ControlProvider<C> solveChallenge(ProblemDefinition problemDef, AvatarDefinition avatarDef) {
        //TODO: Send problem to GUI, wait for user to complete, return provided control

        ProblemInstance problem = new ProblemInstance(problemDef, avatarDef);
        problem.init();
        test.setProblem(problem);
        test.reset();

        while (problem.getStatus() == ProblemInstance.ProblemStatus.RUNNING) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                log.error("Exception occurred while waiting for user to solve challenge");
                e.printStackTrace();
            }
        }

        return problem.getCtrlProvider();
    }
}
