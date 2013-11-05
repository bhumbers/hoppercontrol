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

    protected boolean m_inContact;
    protected float m_springVel;
    protected ControlState m_controlState;

    protected RevoluteJoint m_hipJoint;
    protected PrismaticJoint m_thrustJoint;
    protected PrismaticJoint m_springLineJoint;
    protected DistanceJoint m_springJoint;

    Vec2 m_offset = new Vec2();
    Body m_chassis;
    Body m_upperLeg;
    Body m_lowerLeg;
    boolean m_motorOn;
    float m_motorSpeed;

    public BipedHopper() {
        m_inContact = false;
        m_springVel = 0;
        m_controlState = ControlState.FLIGHT;
    }

    /** Returns body which should be monitored for contact with ground in order to update
     * "inContact" flag
     */
    public Body getGroundContactBody() {return m_lowerLeg;}

    public ControlState getControlState() {return m_controlState;}

    public void setInContact(boolean val) {m_inContact = val;}
    public boolean getInContact() {return m_inContact;}

    public void init(World world) {
        m_offset.set(0.0f, 12.0f);
        m_motorSpeed = 2.0f;
        m_motorOn = true;

        final Vec2 CHASSIS_SIZE = new Vec2(2f, 0.5f);
        final float CHASSIS_DENSITY = 1.0f;

        final Vec2 UPPER_LEG_SIZE = new Vec2(0.4f, 2.0f);
        final float UPPER_LEG_DENSITY = 1.0f;

        final Vec2 LOWER_LEG_SIZE = new Vec2(0.2f, 2.0f);
        final float LOWER_LEG_DENSITY = 1.0f;
        final float LOWER_LEG_KNEE_OFFSET = 0.0f;

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
            bd.position.addLocal(m_offset);
            m_chassis = world.createBody(bd);
            m_chassis.createFixture(sd);
        }

        // Upper leg
        {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(UPPER_LEG_SIZE.x, UPPER_LEG_SIZE.y);

            FixtureDef sd = new FixtureDef();
            sd.density = UPPER_LEG_DENSITY;
            sd.shape = shape;
            sd.filter.groupIndex = -1;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.addLocal(m_offset);
            bd.position.y -= UPPER_LEG_SIZE.y;
            m_upperLeg = world.createBody(bd);
            m_upperLeg.createFixture(sd);
        }


        //Lower leg
        {
            PolygonShape shinShape = new PolygonShape();
            shinShape.setAsBox(LOWER_LEG_SIZE.x, LOWER_LEG_SIZE.y);
            FixtureDef fd = new FixtureDef();
            fd.density = LOWER_LEG_DENSITY;
            fd.shape = shinShape;
            fd.filter.groupIndex = -1;

            CircleShape capShape = new CircleShape();
            capShape.m_p.y = -LOWER_LEG_SIZE.y; //offset to bottom of shin
            capShape.setRadius(LOWER_LEG_SIZE.x);
            FixtureDef capFd = new FixtureDef();
//            capFd.restitution = 1.0f; //assume perfectly elastic bounces
            capFd.density = LOWER_LEG_DENSITY;
            capFd.shape = capShape;
            capFd.filter.groupIndex = -1;

            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.y = m_upperLeg.getPosition().y - LOWER_LEG_SIZE.y - LOWER_LEG_KNEE_OFFSET;
            m_lowerLeg = world.createBody(bd);
            m_lowerLeg.createFixture(fd);
            Fixture capFixture = m_lowerLeg.createFixture(capFd);
        }

        //Hip joint
        {
            RevoluteJointDef jd = new RevoluteJointDef();

            jd.initialize(m_chassis, m_upperLeg, m_offset);
            jd.collideConnected = false;
            jd.motorSpeed = m_motorSpeed;
            jd.maxMotorTorque = 400.0f;
//            jd.enableMotor = m_motorOn;
            m_hipJoint = (RevoluteJoint) world.createJoint(jd);
        }

        //"Knee" joint
        {
            //Constrain motion to lie along hopper leg axis
            {
                PrismaticJointDef jd = new PrismaticJointDef();
                jd.initialize(m_upperLeg, m_lowerLeg, new Vec2(0,UPPER_LEG_SIZE.y).addLocal(m_upperLeg.getPosition()), new Vec2(0, 1.0f));
                jd.collideConnected = false;
                //            jd.maxMotorForce = 500.0f;
                //            jd.motorSpeed = 0.0f;
                //            jd.enableMotor = true;
                jd.enableLimit = true;
                jd.lowerTranslation = 0;
                jd.upperTranslation = LOWER_LEG_SIZE.y;
                m_springLineJoint = (PrismaticJoint) world.createJoint(jd);
            }

            //Add the "springiness" of the knee
            {
                DistanceJointDef jd = new DistanceJointDef();
                jd.bodyA = m_upperLeg;
                jd.bodyB = m_lowerLeg;
                jd.localAnchorA.set(0,0);
                jd.localAnchorB.set(0,0);
                Vec2 p1 = jd.bodyA.getWorldPoint(jd.localAnchorA);
                Vec2 p2 = jd.bodyB.getWorldPoint(jd.localAnchorB);
                Vec2 d = p2.sub(p1);
                jd.length = d.length();
                jd.dampingRatio = 0.0f;
                jd.frequencyHz = 5.0f;
                m_springJoint = (DistanceJoint) world.createJoint(jd);
            }
        }
    }

    public void updateControl() {
        //TODO: Check for control FSM transitions\
        m_springVel = m_springLineJoint.getJointSpeed();
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
                //TODO
                break;
            case LOAD:
                //TODO
                break;
            case COMPRESS:
                //TODO
                break;
            case THRUST:
                //TODO
                break;
            case UNLOAD:
                //TODO
                break;
        }
    }

}
