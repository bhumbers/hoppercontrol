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

    protected RevoluteJoint m_hipJoint;
    protected PrismaticJoint m_upperLegJoint;   //keeps hip & knee aligned along leg direction
    protected DistanceJoint m_thrustSpring;     //used to control leg length & excite lower leg spring during takeoff thrust
    protected PrismaticJoint m_lowerLegJoint;   //keeps knee & foot aligned along leg direction
    protected DistanceJoint m_hopSpring;      //stores & returns bounce energy for hopper

    Vec2 m_offset = new Vec2();
    Body m_chassis;
    Body m_hip;
    Body m_knee;
    Body m_foot;
    boolean m_motorOn;

    public BipedHopper() {
        m_inContact = false;
        m_springVel = 0;
        m_controlState = ControlState.FLIGHT;
    }

    /** Returns body which should be monitored for contact with ground in order to update
     * "inContact" flag
     */
    public Body getGroundContactBody() {return m_foot;}

    public ControlState getControlState() {return m_controlState;}

    public void setInContact(boolean val) {m_inContact = val;}
    public boolean getInContact() {return m_inContact;}

    public void init(World world) {
        m_offset.set(0.0f, 12.0f);
        m_motorOn = true;

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
            m_hip = world.createBody(bd);
            m_hip.createFixture(sd);
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
            bd.position.y =  m_hip.getPosition().y - UPPER_LEG_DEFAULT_LENGTH;   //offset along thigh
            m_knee = world.createBody(bd);
            m_knee.createFixture(fd);
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
            bd.position.y =  m_knee.getPosition().y - LOWER_LEG_DEFAULT_LENGTH;   //offset to bottom of shin
            m_foot = world.createBody(bd);
            m_foot.createFixture(capFd);
        }

        //Rotary hip joint
        {
            RevoluteJointDef jd = new RevoluteJointDef();

            jd.initialize(m_chassis, m_hip, m_offset);
            jd.collideConnected = false;
//            jd.motorSpeed = 0;
//            jd.maxMotorTorque = 400.0f;
//            jd.enableMotor = true;
            jd.enableLimit = true;
            jd.lowerAngle = jd.upperAngle = 0.0f;
            m_hipJoint = (RevoluteJoint) world.createJoint(jd);
        }

        //Linear upper leg thrust
        {
            {
                PrismaticJointDef jd = new PrismaticJointDef();
                jd.initialize(m_hip, m_knee, m_hip.getPosition().add(m_knee.getPosition()).mulLocal(0.5f), new Vec2(0, 1.0f));
                jd.collideConnected = false;
                m_upperLegJoint = (PrismaticJoint) world.createJoint(jd);
            }

            {
                DistanceJointDef jd = new DistanceJointDef();
                jd.bodyA = m_hip;
                jd.bodyB = m_knee;
                jd.localAnchorA.set(0,0);
                jd.localAnchorB.set(0,0);
                Vec2 p1 = jd.bodyA.getWorldPoint(jd.localAnchorA);
                Vec2 p2 = jd.bodyB.getWorldPoint(jd.localAnchorB);
                Vec2 d = p2.sub(p1);
                jd.length = d.length();
                jd.dampingRatio = 0.8f;
                jd.frequencyHz = 100;
                m_thrustSpring = (DistanceJoint) world.createJoint(jd);
            }
        }

        //Lower leg spring
        {
            //Constrain motion to lie along hopper leg axis
            {
                PrismaticJointDef jd = new PrismaticJointDef();
                jd.initialize(m_knee, m_foot, m_knee.getPosition().add(m_foot.getPosition()).mulLocal(0.5f), new Vec2(0, 1.0f));
                jd.collideConnected = false;
                jd.enableLimit = true;
                //Mechanical stop: Prevent spring from shrinking below some static length
                jd.lowerTranslation = 0;
                jd.upperTranslation = LOWER_LEG_DEFAULT_LENGTH; //slight offset to prevent singular config;
                m_lowerLegJoint = (PrismaticJoint) world.createJoint(jd);
            }

            //Add the "springiness"
            {
                DistanceJointDef jd = new DistanceJointDef();
                jd.bodyA = m_knee;
                jd.bodyB = m_foot;
                jd.localAnchorA.set(0,0);
                jd.localAnchorB.set(0,0);
                Vec2 p1 = jd.bodyA.getWorldPoint(jd.localAnchorA);
                Vec2 p2 = jd.bodyB.getWorldPoint(jd.localAnchorB);
                Vec2 d = p2.sub(p1);
                jd.length = d.length();
                jd.dampingRatio = 0.0f;
                jd.frequencyHz = 15.0f;
                m_hopSpring = (DistanceJoint) world.createJoint(jd);
            }
        }
    }

    public void updateControl() {
        //TODO: Check for control FSM transitions\
        m_springVel = m_lowerLegJoint.getJointSpeed();
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
                if (m_inContact == false)
                    m_controlState = ControlState.FLIGHT;
                break;
        }

        //Run control logic based on FSM state
        switch (m_controlState) {
            case FLIGHT:
                //Retract the leg
                m_thrustSpring.setLength(UPPER_LEG_DEFAULT_LENGTH);
                break;
            case LOAD:
                //TODO
                break;
            case COMPRESS:
                //TODO
                break;
            case THRUST:
                //Add thrust back by pushing down on spring
                //TODO: Correct thrust for desired hop height
                m_thrustSpring.setLength(UPPER_LEG_DEFAULT_LENGTH + 0.3f);
                break;
            case UNLOAD:
                //TODO
                break;
        }
    }

}
