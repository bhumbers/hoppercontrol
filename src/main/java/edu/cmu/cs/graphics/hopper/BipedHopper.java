package edu.cmu.cs.graphics.hopper;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.joints.*;

/** Holds body state and runs control logic for biped hopper avatar */
public class BipedHopper {

    public enum ControlState {
        FLIGHT,
        LOAD,
        COMPRESS,
        THRUST,
        UNLOAD
    }

    final int NUM_LEGS = 1;

    private final float HIP_PROP_GAIN = 1.0f;
    private final float HIP_DRAG_GAIN = 20.0f;

    private final Vec2 CHASSIS_SIZE = new Vec2(2f, 0.5f);
    private final float CHASSIS_DENSITY = 0.1f;

    private final Vec2 HIP_SIZE = new Vec2(0.3f, 0.1f);
    private final float HIP_DENSITY = 1.0f;
    private final float UPPER_LEG_DEFAULT_LENGTH = 3.0f;

    private final Vec2 KNEE_SIZE = new Vec2(0.2f, 0.1f);
    private final float KNEE_DENSITY = 1.0f;
    private final float LOWER_LEG_DEFAULT_LENGTH = 2.0f;

    private final float FOOT_RADIUS = 0.2f;
    private final float FOOT_DENSITY = 1.0f;

    protected boolean m_inContact;
    protected float m_springVel;
    protected ControlState m_controlState;
    protected Vec2 m_bodyVel;
    protected float m_bodyPitch;

    protected float m_currSupportPeriod;             //running count of time length of current support period (or 0 if not in support)
    protected float m_nextSupportPeriodEst;          //estimate of length of next period that active leg is touching ground

    //Joints (arrays where each index corresponds to one of the legs)
    protected RevoluteJoint m_hipJoint[];
    protected PrismaticJoint m_upperLegJoint[];    //keeps hip & knee aligned along leg direction
    protected DistanceJoint m_thrustSpring[];       //used to control leg length & excite lower leg spring during takeoff thrust
    protected PrismaticJoint m_lowerLegJoint[];     //keeps knee & foot aligned along leg direction
    protected DistanceJoint m_hopSpring[];          //stores & returns bounce energy for hopper

    Vec2 m_offset = new Vec2();
    Body m_chassis;
    Body m_hip[];
    Body m_knee[];
    Body m_foot[];

    protected float m_targetBodyVelX = 0.0f;
    protected float m_desiredHipPitch = 0.0f;

    protected int m_activeLegIdx;
    protected int m_idleLegIdx;

    public BipedHopper() {
        m_inContact = false;
        m_springVel = 0;
        m_controlState = ControlState.FLIGHT;
        m_bodyVel = new Vec2();
        m_bodyPitch = 0.0f;
        m_activeLegIdx = 0; m_idleLegIdx = 1;

        m_currSupportPeriod = 0.0f;
        m_nextSupportPeriodEst = 1.0f; //TODO: What's a reasonable init value for this?

        m_hip = new Body[NUM_LEGS];
        m_knee = new Body[NUM_LEGS];
        m_foot = new Body[NUM_LEGS];
        m_hipJoint = new RevoluteJoint[NUM_LEGS];
        m_upperLegJoint = new PrismaticJoint[NUM_LEGS];
        m_thrustSpring = new DistanceJoint[NUM_LEGS];
        m_lowerLegJoint = new PrismaticJoint[NUM_LEGS];
        m_hopSpring = new DistanceJoint[NUM_LEGS];
    }

    /** Returns body which should be monitored for contact with ground in order to update
     * "inContact" flag
     */
    public Body getGroundContactBody() {return m_foot[m_activeLegIdx];}

    public ControlState getControlState() {return m_controlState;}

    public void setInContact(boolean val) {m_inContact = val;}
    public boolean getInContact() {return m_inContact;}

    public void init(World world) {
        m_offset.set(0.0f, 12.0f);

        // Chassis
        {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(CHASSIS_SIZE.x, CHASSIS_SIZE.y);

            FixtureDef sd = new FixtureDef();
            sd.density = CHASSIS_DENSITY;
            sd.shape = shape;
            sd.filter.groupIndex = -1;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.set(m_offset);
            m_chassis = world.createBody(bd);
            m_chassis.createFixture(sd);
        }

        for (int i = 0; i < NUM_LEGS; i++) {
            // Hip
            {
                PolygonShape shape = new PolygonShape();
                shape.setAsBox(HIP_SIZE.x, HIP_SIZE.y);

                FixtureDef sd = new FixtureDef();
                sd.density = HIP_DENSITY;
                sd.shape = shape;
                sd.filter.groupIndex = -1;
                BodyDef bd = new BodyDef();
                bd.type = BodyType.DYNAMIC;
                bd.position.set(m_offset);
                m_hip[i] = world.createBody(bd);
                m_hip[i].createFixture(sd);
            }


            //Knee
            {
                PolygonShape shinShape = new PolygonShape();
                shinShape.setAsBox(KNEE_SIZE.x, KNEE_SIZE.y);
                FixtureDef fd = new FixtureDef();
                fd.density = KNEE_DENSITY;
                fd.shape = shinShape;
                fd.filter.groupIndex = -1;

                BodyDef bd = new BodyDef();
                bd.type = BodyType.DYNAMIC;
                bd.position.y =  m_hip[i].getPosition().y - UPPER_LEG_DEFAULT_LENGTH;   //offset along thigh
                m_knee[i] = world.createBody(bd);
                m_knee[i].createFixture(fd);
            }

            //Foot
            {
                CircleShape footShape = new CircleShape();
                footShape.setRadius(FOOT_RADIUS);
                FixtureDef capFd = new FixtureDef();
                capFd.density = FOOT_DENSITY;
                capFd.shape = footShape;
                capFd.filter.groupIndex = -1;

                BodyDef bd = new BodyDef();
                bd.type = BodyType.DYNAMIC;
                bd.position.y =  m_knee[i].getPosition().y - LOWER_LEG_DEFAULT_LENGTH;   //offset to bottom of shin
                m_foot[i] = world.createBody(bd);
                m_foot[i].createFixture(capFd);
            }

            //Rotary hip joint
            {
                RevoluteJointDef jd = new RevoluteJointDef();

                jd.initialize(m_chassis, m_hip[i], m_offset);
                jd.collideConnected = false;
    //            jd.enableLimit = true;
    //            jd.lowerAngle = jd.upperAngle = 0.0f;
                m_hipJoint[i] = (RevoluteJoint) world.createJoint(jd);
            }

            //Linear upper leg thrust
            {
                {
                    PrismaticJointDef jd = new PrismaticJointDef();
                    jd.initialize(m_hip[i], m_knee[i], m_hip[i].getPosition().add(m_knee[i].getPosition()).mulLocal(0.5f), new Vec2(0, 1.0f));
                    jd.collideConnected = false;
                    m_upperLegJoint[i] = (PrismaticJoint) world.createJoint(jd);
                }

                {
                    DistanceJointDef jd = new DistanceJointDef();
                    jd.bodyA = m_hip[i];
                    jd.bodyB = m_knee[i];
                    jd.localAnchorA.set(0,0);
                    jd.localAnchorB.set(0,0);
                    Vec2 p1 = jd.bodyA.getWorldPoint(jd.localAnchorA);
                    Vec2 p2 = jd.bodyB.getWorldPoint(jd.localAnchorB);
                    Vec2 d = p2.sub(p1);
                    jd.length = d.length();
                    jd.dampingRatio = 0.8f;
                    jd.frequencyHz = 100;
                    m_thrustSpring[i] = (DistanceJoint) world.createJoint(jd);
                }
            }

            //Lower leg spring
            {
                //Constrain motion to lie along hopper leg axis
                {
                    PrismaticJointDef jd = new PrismaticJointDef();
                    jd.initialize(m_knee[i], m_foot[i], m_knee[i].getPosition().add(m_foot[i].getPosition()).mulLocal(0.5f), new Vec2(0, 1.0f));
                    jd.collideConnected = false;
                    jd.enableLimit = true;
                    //Mechanical stop: Prevent spring from shrinking below some static length
                    jd.lowerTranslation = 0;
                    jd.upperTranslation = LOWER_LEG_DEFAULT_LENGTH; //slight offset to prevent singular config;
                    m_lowerLegJoint[i] = (PrismaticJoint) world.createJoint(jd);
                }

                //Add the "springiness"
                {
                    DistanceJointDef jd = new DistanceJointDef();
                    jd.bodyA = m_knee[i];
                    jd.bodyB = m_foot[i];
                    jd.localAnchorA.set(0,0);
                    jd.localAnchorB.set(0,0);
                    Vec2 p1 = jd.bodyA.getWorldPoint(jd.localAnchorA);
                    Vec2 p2 = jd.bodyB.getWorldPoint(jd.localAnchorB);
                    Vec2 d = p2.sub(p1);
                    jd.length = d.length();
                    jd.dampingRatio = 0.0f;
                    jd.frequencyHz = 5.0f;
                    m_hopSpring[i] = (DistanceJoint) world.createJoint(jd);
                }
            }
        }
    }

    /** Updates control & actuation for this hopper based on given simulation timestep dt (in seconds) */
    public void updateControl(float dt) {
        //Update sensor values
        m_springVel = m_lowerLegJoint[m_activeLegIdx].getJointSpeed();
        m_bodyVel = m_chassis.getLinearVelocity();
        m_bodyPitch = m_chassis.getAngle();

        //Check for control FSM transitions
        switch (m_controlState) {
            case FLIGHT:
                //Switch to load or compress state once we make contact with ground
                if (m_inContact)
                    m_controlState = ControlState.COMPRESS; //TODO: include "LOAD" phase as well?
                break;
            case COMPRESS:
                //Switch to thrusting once at or past fully compressed spring
                if (m_springVel < 0)
                    m_controlState = ControlState.THRUST;
                break;
            case THRUST:
                //Switch to flight once we leave the ground
                if (m_inContact == false) {
                    m_controlState = ControlState.FLIGHT;
                    m_nextSupportPeriodEst = m_currSupportPeriod; //estimate next support from current
                    m_currSupportPeriod = 0.0f;
                    swapActiveLeg();
                }
                break;
        }

        //Run control logic based on FSM state
        switch (m_controlState) {
            case FLIGHT:
                servoLegPlacement(dt);
                break;
            case LOAD:
                //TODO
                break;
            case COMPRESS:
                //TODO
                servoActiveHipPitch();
                break;
            case THRUST:
                //Add thrust back by pushing down on spring
                //TODO: Correct thrust for desired hop height
                m_thrustSpring[m_activeLegIdx].setLength(UPPER_LEG_DEFAULT_LENGTH + 0.3f);
                break;
            case UNLOAD:
                //TODO
                break;
        }

        if (m_inContact)
            m_currSupportPeriod += dt;

        //Idle hip is constantly servoed throughout control cycle
        if (NUM_LEGS > 1)
            servoIdleHipPitch();
    }

    protected void swapActiveLeg() {
        if (NUM_LEGS == 2) {
            int temp = m_idleLegIdx;
            m_idleLegIdx = m_activeLegIdx;
            m_activeLegIdx = temp;
        }
    }

    protected void servoActiveHipPitch() {
        servoTowardAngle(m_hipJoint[m_activeLegIdx], m_desiredHipPitch, HIP_PROP_GAIN, HIP_DRAG_GAIN);
    }

    protected void servoIdleHipPitch() {
        //Mirror the active hip by servo-ing to negative of its pitch
        float activeHipPitch = m_hipJoint[m_activeLegIdx].getJointAngle();
        float targetIdleHipPitch = -activeHipPitch;
        servoTowardAngle(m_hipJoint[m_idleLegIdx], targetIdleHipPitch, HIP_PROP_GAIN, HIP_DRAG_GAIN);
    }

    protected void servoLegPlacement(float dt) {
        /////// LENGTHS /////////////////////////////////////////////////////////////////////////////
        //Retract idle leg, lengthen active for landing
        //(to make this gradual, use lerp on current value (hacky, but seems to work well))
        float alpha = Math.min(1.0f, 6.0f * dt); //~0.1 for 60 Hz updates

        float idleLegTargetLength = UPPER_LEG_DEFAULT_LENGTH - 2.0f;
        float activeLegTargetLength = UPPER_LEG_DEFAULT_LENGTH + 0.3f;

        if (NUM_LEGS > 1)
            m_thrustSpring[m_idleLegIdx].setLength(lerp(m_thrustSpring[m_idleLegIdx].getLength(), idleLegTargetLength, alpha));
        m_thrustSpring[m_activeLegIdx].setLength(lerp(m_thrustSpring[m_activeLegIdx].getLength(), activeLegTargetLength, alpha));
        /////////////////////////////////////////////////////////////////////////////////////////////

        /////// ANGLE /////////////////////////////////////////////////////////////////////////////
        //Set leg position using hip based on desired landing location
        float deltaFromTargetVel = m_bodyVel.x - m_targetBodyVelX;
        float targetVelGain = 0.5f;
        float desiredLandingOffsetX = (0.5f * m_bodyVel.x * m_nextSupportPeriodEst) + (targetVelGain * deltaFromTargetVel);

        //Bound to some reasonable range
        float maxAllowedOffsetX = 0.5f * activeLegTargetLength;
        if (desiredLandingOffsetX > maxAllowedOffsetX)
            desiredLandingOffsetX = maxAllowedOffsetX;
        if (desiredLandingOffsetX < -maxAllowedOffsetX)
            desiredLandingOffsetX = -maxAllowedOffsetX;

        float targetActiveHipAngle =  0.0f;
        float eps = 0.000001f;
        if (Math.abs(desiredLandingOffsetX) > eps)
            targetActiveHipAngle = -m_bodyPitch;// + (float)(Math.asin(desiredLandingOffsetX/activeLegTargetLength));

        if (Float.isNaN(targetActiveHipAngle)) {
            targetActiveHipAngle = 0.0f;
        }

        servoTowardAngle(m_hipJoint[m_activeLegIdx], targetActiveHipAngle, HIP_PROP_GAIN, HIP_DRAG_GAIN);
        /////////////////////////////////////////////////////////////////////////////////////////////
    }

    //Returns (1-alpha)*x + alpha*y
    protected float lerp(float x, float y, float alpha) {
        return (1-alpha)*x + alpha*y;
    }

    protected void servoTowardAngle(RevoluteJoint joint, float targetAngle, float propGain, float dragGain)     {
        float jointAngle = joint.getJointAngle();
        float jointSpeed = joint.getJointSpeed();
        float targetJointDelta = targetAngle - jointAngle;

        final float BIG_NUMBER = Float.MAX_VALUE;

        float torque = propGain*targetJointDelta - dragGain*jointSpeed;

        //Hacky, but get joint to use our torque by setting our torque as max and forcing use of max torque
        //by setting some arbitrarily large velocity in servo direction
        torque = Math.abs(torque);
        m_hipJoint[m_activeLegIdx].enableMotor(true);
        m_hipJoint[m_activeLegIdx].setMaxMotorTorque(torque);
        m_hipJoint[m_activeLegIdx].setMotorSpeed(targetJointDelta > 0 ? BIG_NUMBER : -BIG_NUMBER);
    }

}