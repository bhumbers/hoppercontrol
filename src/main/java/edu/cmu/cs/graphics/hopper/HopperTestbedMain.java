package edu.cmu.cs.graphics.hopper;

import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.BipedHopperDefinition;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.control.ControlProviderDefinition;
import edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests.BipedHopperTest;
import edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests.ProblemInstanceTest;
import edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests.WormTest;
import edu.cmu.cs.graphics.hopper.eval.BipedObstacleEvaluatorDefinition;
import edu.cmu.cs.graphics.hopper.problems.ObstacleProblemDefinition;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import org.jbox2d.testbed.framework.*;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;

import javax.swing.*;
import java.util.Arrays;

/** Main UI entry point for hopper project
 * Automated tasks without UI may have other entry points
 * */
public class HopperTestbedMain {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
//      log.warn("Could not set the look and feel to nimbus.  "
//          + "Hopefully you're on a mac so the window isn't ugly as crap.");
        }
        TestbedModel model = new TestbedModel();

        //Not fun, but the hopper avatar requires a *lot* of iterations/short timestep to converge
        //Hoping this can be reduced by tweaking how we use joints, but not sure...
        model.getSettings().getSetting(TestbedSettings.PositionIterations).value = 30;
        model.getSettings().getSetting(TestbedSettings.VelocityIterations).value = 50;

        TestbedPanel panel = new TestPanelJ2D(model);

        ProblemInstanceTest piTest = new ProblemInstanceTest();
        ProblemInstance problem = new ProblemInstance((new ObstacleProblemDefinition(1.0f, 1.0f)),
                                                        new BipedHopperDefinition(),
                                                        new BipedObstacleEvaluatorDefinition(30.0f, 20.0f, 1.0f, 5.0f),
                                                        new ControlProviderDefinition<BipedHopperControl>(Arrays.asList(new BipedHopperControl())));
        model.addTest(new BipedHopperTest());
        problem.init();
        model.addTest(piTest);
        piTest.setProblem(problem);

        model.addTest(new WormTest());

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

}
