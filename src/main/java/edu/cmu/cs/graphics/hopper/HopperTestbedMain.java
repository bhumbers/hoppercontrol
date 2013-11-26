package edu.cmu.cs.graphics.hopper;

import edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests.BipedHopperTest;
import edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests.WormTest;
import org.jbox2d.testbed.framework.*;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;

import javax.swing.*;

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

        //TestList.populateModel(model);
        model.addTest(new WormTest());
        model.addTest(new BipedHopperTest());

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
