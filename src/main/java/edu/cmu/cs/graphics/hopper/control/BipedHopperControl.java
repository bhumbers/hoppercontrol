package edu.cmu.cs.graphics.hopper.control;

import java.text.DecimalFormat;

/** Control parameters specific to a biped hopper */
public class BipedHopperControl extends Control {
    private final float DEFAULT_ACTIVE_THRUST_LENGTH_DELTA = 0.002f;  //push down on spring during thrust
    private final float DEFAULT_IDLE_THRUST_LENGTH_DELTA = -2.5f;   //tuck away
    private final float DEFAULT_TARGET_VELOCITY_LEG_PLACEMENT_GAIN = 0.1f;

    public float m_activeThrustDelta;       //offset to thrust piston used during thrust phase (affects hopping height)
    public float m_idleThrustDelta;
    public float m_targetBodyVelX;
    public float m_targetBodyPitch;         //target angle of main hopper body *relative to world coordinate frame*
    public float m_targetBodyVelXLegPlacementGain;

    //For debug string output
    static DecimalFormat strFormat = new DecimalFormat( "#,###,###,##0.000" );

    public BipedHopperControl() {
        m_activeThrustDelta = DEFAULT_ACTIVE_THRUST_LENGTH_DELTA;
        m_idleThrustDelta = DEFAULT_IDLE_THRUST_LENGTH_DELTA;
        m_targetBodyVelX = 0.0f;
        m_targetBodyPitch = 0.0f;
        m_targetBodyVelXLegPlacementGain = DEFAULT_TARGET_VELOCITY_LEG_PLACEMENT_GAIN;
    }

    @Override
    public Control duplicate() {
        BipedHopperControl copy = new BipedHopperControl();
        copy.m_activeThrustDelta = this.m_activeThrustDelta;
        copy.m_idleThrustDelta = this.m_idleThrustDelta;
        copy.m_targetBodyVelX = this.m_targetBodyVelX;
        copy.m_targetBodyPitch = this.m_targetBodyPitch;
        copy.m_targetBodyVelXLegPlacementGain = m_targetBodyVelXLegPlacementGain;
        return copy;
    }

    @Override
    public String toString() {
        String str = "";
        str += "Active Thrust:  " + strFormat.format(m_activeThrustDelta) + " ";
        str += "Idle Thrust:    " + strFormat.format(m_idleThrustDelta) + " ";
        str += "Target VelX:    " + strFormat.format(m_targetBodyVelX) + " ";
        str += "Target Pitch:   " + strFormat.format(m_targetBodyPitch) + " ";
        str += "Leg Place Gain: " + strFormat.format(m_targetBodyVelXLegPlacementGain) + " ";
        return str;
    }
}
