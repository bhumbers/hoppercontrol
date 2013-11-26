package edu.cmu.cs.graphics.hopper.control;

import org.jbox2d.dynamics.joints.RevoluteJoint;

public class ControlUtils {

    /** Calculates servo torque for revolute joint toward given angle using specified gains.
     * Returns applied torque. */
    public static float servoTowardAngle(RevoluteJoint joint, float targetAngle, float propGain, float dragGain)     {
        float jointAngle = joint.getJointAngle();
        float jointSpeed = joint.getJointSpeed();
        float targetJointDelta = targetAngle - jointAngle;

        final float BIG_NUMBER = Float.MAX_VALUE;

        float torque = propGain*targetJointDelta - dragGain*jointSpeed;

        //Hacky, but get joint to use our torque by setting our torque as max and forcing use of max torque
        //by setting some arbitrarily large velocity in servo direction
        float absTorque = Math.abs(torque);
        float signTorque = Math.signum(torque);
        joint.enableMotor(true);
        joint.setMaxMotorTorque(absTorque);
        joint.setMotorSpeed(signTorque > 0 ? BIG_NUMBER : -BIG_NUMBER);

        return torque;
    }
}
