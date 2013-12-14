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
        copy.fillFromNumericArray(this.toNumericArray());
        return copy;
    }

    @Override
    public void fillFromNumericArray(float[] vals) {
        int valIdx = 0;
        this.activeThrustDelta = vals[valIdx++];
        this.idleThrustDelta = vals[valIdx++];
        this.targetBodyVelX = vals[valIdx++];
        this.targetBodyPitch = vals[valIdx++];
        this.targetBodyVelXLegPlacementGain = vals[valIdx++];
    }

    @Override
    public float[] toNumericArray() {
        float[] vals = new float[5];
        int valIdx = 0;
        vals[valIdx++] = this.activeThrustDelta;
        vals[valIdx++] = this.idleThrustDelta;
        vals[valIdx++] = this.targetBodyVelX;
        vals[valIdx++] = this.targetBodyPitch;
        vals[valIdx++] = this.targetBodyVelXLegPlacementGain;
        return vals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BipedHopperControl that = (BipedHopperControl) o;

        if (Float.compare(that.activeThrustDelta, activeThrustDelta) != 0) return false;
        if (Float.compare(that.idleThrustDelta, idleThrustDelta) != 0) return false;
        if (Float.compare(that.targetBodyPitch, targetBodyPitch) != 0) return false;
        if (Float.compare(that.targetBodyVelX, targetBodyVelX) != 0) return false;
        if (Float.compare(that.targetBodyVelXLegPlacementGain, targetBodyVelXLegPlacementGain) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (activeThrustDelta != +0.0f ? Float.floatToIntBits(activeThrustDelta) : 0);
        result = 31 * result + (idleThrustDelta != +0.0f ? Float.floatToIntBits(idleThrustDelta) : 0);
        result = 31 * result + (targetBodyVelX != +0.0f ? Float.floatToIntBits(targetBodyVelX) : 0);
        result = 31 * result + (targetBodyPitch != +0.0f ? Float.floatToIntBits(targetBodyPitch) : 0);
        result = 31 * result + (targetBodyVelXLegPlacementGain != +0.0f ? Float.floatToIntBits(targetBodyVelXLegPlacementGain) : 0);
        return result;
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
