package edu.cmu.cs.graphics.hopper.control;

import org.jbox2d.common.Vec2;

/** A serializable POJO state/config/pose for the biped hopper */
public class BipedHopperState {
    Vec2 x;
    Vec2 xdot;
    double theta;
    double thetadot;
    public float[] joints;          //current DOF values
    public float[] jointVels;       //current DOF velocities
    public BipedHopper.ControlState controlState;
}
