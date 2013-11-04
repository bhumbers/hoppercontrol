package edu.cmu.cs.graphics.hopper;

import edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests.BipedHopperTest;
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
        TestbedPanel panel = new TestPanelJ2D(model);

        //TestList.populateModel(model);
        model.addTest(new BipedHopperTest());

        JFrame testbed = new org.jbox2d.testbed.framework.TestbedFrame(model, panel, TestbedController.UpdateBehavior.UPDATE_CALLED);
        testbed.setVisible(true);
        testbed.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
