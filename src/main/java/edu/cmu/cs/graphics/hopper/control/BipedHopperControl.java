package edu.cmu.cs.graphics.hopper.control;

import java.text.DecimalFormat;

/** Control parameters specific to a biped hopper */
public class BipedHopperControl extends Control {
    private static final float DEFAULT_ACTIVE_THRUST_LENGTH_DELTA = 0.3f;  //push down on spring during thrust
    private static final float DEFAULT_IDLE_THRUST_LENGTH_DELTA = -2.5f;   //tuck away
    private static final float DEFAULT_TARGET_VELOCITY_LEG_PLACEMENT_GAIN = 0.1f;

    public float activeThrustDelta;       //offset to thrust piston used during thrust phase (affects hopping height)
    public float idleThrustDelta;
    public float targetBodyVelX;
    public float targetBodyPitch;         //target angle of main hopper body *relative to world coordinate frame*
    public float targetBodyVelXLegPlacementGain;

    //For debug string output
    static DecimalFormat strFormat = new DecimalFormat( "#,###,###,##0.000" );

    public BipedHopperControl() {
        activeThrustDelta = DEFAULT_ACTIVE_THRUST_LENGTH_DELTA;
        idleThrustDelta = DEFAULT_IDLE_THRUST_LENGTH_DELTA;
        targetBodyVelX = 0.0f;
        targetBodyPitch = 0.0f;
        targetBodyVelXLegPlacementGain = DEFAULT_TARGET_VELOCITY_LEG_PLACEMENT_GAIN;
    }

    @Override
    public Control duplicate() {
        BipedHopperControl copy = new BipedHopperControl();
        copy.activeThrustDelta = this.activeThrustDelta;
        copy.idleThrustDelta = this.idleThrustDelta;
        copy.targetBodyVelX = this.targetBodyVelX;
        copy.targetBodyPitch = this.targetBodyPitch;
        copy.targetBodyVelXLegPlacementGain = targetBodyVelXLegPlacementGain;
        return copy;
    }

    @Override
    public Control fromNumericArray(float[] vals) {
        BipedHopperControl control = new BipedHopperControl();
        int valIdx = 0;
        control.activeThrustDelta = vals[valIdx++];
        control.idleThrustDelta = vals[valIdx++];
        control.targetBodyVelX = vals[valIdx++];
        control.targetBodyPitch = vals[valIdx++];
        control.targetBodyVelXLegPlacementGain = vals[valIdx++];
        return control;
    }

    @Override
    public String toString() {
        String str = "";
        str += "Active Thrust:  " + strFormat.format(activeThrustDelta) + " ";
        str += "Idle Thrust:    " + strFormat.format(idleThrustDelta) + " ";
        str += "Target VelX:    " + strFormat.format(targetBodyVelX) + " ";
        str += "Target Pitch:   " + strFormat.format(targetBodyPitch) + " ";
        str += "Leg Place Gain: " + strFormat.format(targetBodyVelXLegPlacementGain) + " ";
        return str;
    }
}
