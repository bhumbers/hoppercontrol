package edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;


/** Main scene setup and control update class for BipedHoppper
 * Modeled on the TheoJansenTest from JBox2D TestBed.
 */
public class BipedHopperTest extends TestbedTest {
    private static final long CHASSIS_TAG = 1;
    private static final long UPPER_LEG_TAG = 2;
    private static final long LOWER_LEG_TAG = 8;
    private static final long HIP_TAG = 16;
    private static final long KNEE_TAG = 16;

    Vec2 m_offset = new Vec2();
    Body m_chassis;
    Body m_upperLeg;
    Body m_lowerLeg;
    RevoluteJoint m_hipJoint;
    PrismaticJoint m_kneeJoint;
    DistanceJoint m_springJoint;
    boolean m_motorOn;
    float m_motorSpeed;

    @Override
    public Long getTag(Body argBody) {
        if (argBody == m_chassis) {
            return CHASSIS_TAG;
        } else if (argBody == m_upperLeg) {
            return UPPER_LEG_TAG;
        } else if (argBody == m_lowerLeg) {
            return LOWER_LEG_TAG;
        }
        return null;
    }

    @Override
    public Long getTag(Joint argJoint) {
        if (argJoint == m_hipJoint) {
            return HIP_TAG;
        }
        return null;
    }

    @Override
    public void processBody(Body argBody, Long argTag) {
        if (argTag == CHASSIS_TAG) {
            m_chassis = argBody;
        } else if (argTag == UPPER_LEG_TAG) {
            m_upperLeg = argBody;
        } else if (argTag == LOWER_LEG_TAG) {
             m_lowerLeg = argBody;
        }
    }

    @Override
    public void processJoint(Joint argJoint, Long argTag) {
        if (argTag == HIP_TAG) {
            m_hipJoint = (RevoluteJoint) argJoint;
            m_motorOn = m_hipJoint.isMotorEnabled();
        }
    }

    @Override
    public boolean isSaveLoadEnabled() {
        return true;
    }

    @Override
    public void initTest(boolean argDeserialized) {
        if (argDeserialized) {
            return;
        }

        m_offset.set(0.0f, 12.0f);
        m_motorSpeed = 2.0f;
        m_motorOn = true;

        // Ground
        {
            BodyDef bd = new BodyDef();
            Body ground = getWorld().createBody(bd);

            EdgeShape shape = new EdgeShape();
            shape.set(new Vec2(-50.0f, 0.0f), new Vec2(50.0f, 0.0f));
            ground.createFixture(shape, 0.0f);

            shape.set(new Vec2(-50.0f, 0.0f), new Vec2(-50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);

            shape.set(new Vec2(50.0f, 0.0f), new Vec2(50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);
        }

//        // Balls
//        for (int i = 0; i < 40; ++i) {
//            CircleShape shape = new CircleShape();
//            shape.m_radius = 0.25f;
//
//            BodyDef bd = new BodyDef();
//            bd.type = BodyType.DYNAMIC;
//            bd.position.set(-40.0f + 2.0f * i, 0.5f);
//
//            Body body = getWorld().createBody(bd);
//            body.createFixture(shape, 1.0f);
//        }

        final Vec2 CHASSIS_SIZE = new Vec2(2f, 0.5f);
        final float CHASSIS_DENSITY = 1.0f;

        final Vec2 UPPER_LEG_SIZE = new Vec2(0.4f, 2.0f);
        final float UPPER_LEG_DENSITY = 1.0f;

        final Vec2 LOWER_LEG_SIZE = new Vec2(0.2f, 2.0f);
        final float LOWER_LEG_DENSITY = 1.0f;
        final float LOWER_LEG_KNEE_OFFSET = 0.5f;

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
            m_chassis = getWorld().createBody(bd);
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
            m_upperLeg = getWorld().createBody(bd);
            m_upperLeg.createFixture(sd);
        }


        //Lower leg
        {
            PolygonShape shape = new PolygonShape();
            shape.setAsBox(LOWER_LEG_SIZE.x, LOWER_LEG_SIZE.y);

            FixtureDef sd = new FixtureDef();
            sd.density = LOWER_LEG_DENSITY;
            sd.shape = shape;
            sd.filter.groupIndex = -1;
            BodyDef bd = new BodyDef();
            bd.type = BodyType.DYNAMIC;
            bd.position.y = m_upperLeg.getPosition().y - LOWER_LEG_SIZE.y - LOWER_LEG_KNEE_OFFSET;
            m_lowerLeg = getWorld().createBody(bd);
            m_lowerLeg.createFixture(sd);
        }

        //Hip joint
        {
            RevoluteJointDef jd = new RevoluteJointDef();

            jd.initialize(m_chassis, m_upperLeg, m_offset);
            jd.collideConnected = false;
            jd.motorSpeed = m_motorSpeed;
            jd.maxMotorTorque = 400.0f;
//            jd.enableMotor = m_motorOn;
            m_hipJoint = (RevoluteJoint) getWorld().createJoint(jd);
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
    //            jd.enableLimit = true;
    //            jd.upperTranslation = jd.lowerTranslation = 0;
                m_kneeJoint = (PrismaticJoint) getWorld().createJoint(jd);
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
                jd.dampingRatio = 0.1f;
                jd.frequencyHz = 5.0f;
                m_springJoint = (DistanceJoint) getWorld().createJoint(jd);
            }
        }
    }

//    void createLeg(float s, Vec2 wheelAnchor) {
//        Vec2 p1 = new Vec2(5.4f * s, -6.1f);
//        Vec2 p2 = new Vec2(7.2f * s, -1.2f);
//        Vec2 p3 = new Vec2(4.3f * s, -1.9f);
//        Vec2 p4 = new Vec2(3.1f * s, 0.8f);
//        Vec2 p5 = new Vec2(6.0f * s, 1.5f);
//        Vec2 p6 = new Vec2(2.5f * s, 3.7f);
//
//        FixtureDef fd1 = new FixtureDef();
//        FixtureDef fd2 = new FixtureDef();
//        fd1.filter.groupIndex = -1;
//        fd2.filter.groupIndex = -1;
//        fd1.density = 1.0f;
//        fd2.density = 1.0f;
//
//        PolygonShape poly1 = new PolygonShape();
//        PolygonShape poly2 = new PolygonShape();
//
//        if (s > 0.0f) {
//            Vec2[] vertices = new Vec2[3];
//
//            vertices[0] = p1;
//            vertices[1] = p2;
//            vertices[2] = p3;
//            poly1.set(vertices, 3);
//
//            vertices[0] = new Vec2();
//            vertices[1] = p5.sub(p4);
//            vertices[2] = p6.sub(p4);
//            poly2.set(vertices, 3);
//        } else {
//            Vec2[] vertices = new Vec2[3];
//
//            vertices[0] = p1;
//            vertices[1] = p3;
//            vertices[2] = p2;
//            poly1.set(vertices, 3);
//
//            vertices[0] = new Vec2();
//            vertices[1] = p6.sub(p4);
//            vertices[2] = p5.sub(p4);
//            poly2.set(vertices, 3);
//        }
//
//        fd1.shape = poly1;
//        fd2.shape = poly2;
//
//        BodyDef bd1 = new BodyDef(), bd2 = new BodyDef();
//        bd1.type = BodyType.DYNAMIC;
//        bd2.type = BodyType.DYNAMIC;
//        bd1.position = m_offset;
//        bd2.position = p4.add(m_offset);
//
//        bd1.angularDamping = 10.0f;
//        bd2.angularDamping = 10.0f;
//
//        Body body1 = getWorld().createBody(bd1);
//        Body body2 = getWorld().createBody(bd2);
//
//        body1.createFixture(fd1);
//        body2.createFixture(fd2);
//
//        DistanceJointDef djd = new DistanceJointDef();
//
//        // Using a soft distance constraint can reduce some jitter.
//        // It also makes the structure seem a bit more fluid by
//        // acting like a suspension system.
//        djd.dampingRatio = 0.5f;
//        djd.frequencyHz = 10.0f;
//
//        djd.initialize(body1, body2, p2.add(m_offset), p5.add(m_offset));
//        getWorld().createJoint(djd);
//
//        djd.initialize(body1, body2, p3.add(m_offset), p4.add(m_offset));
//        getWorld().createJoint(djd);
//
//        djd.initialize(body1, m_wheel, p3.add(m_offset), wheelAnchor.add(m_offset));
//        getWorld().createJoint(djd);
//
//        djd.initialize(body2, m_wheel, p6.add(m_offset), wheelAnchor.add(m_offset));
//        getWorld().createJoint(djd);
//
//        RevoluteJointDef rjd = new RevoluteJointDef();
//
//        rjd.initialize(body2, m_chassis, p4.add(m_offset));
//        getWorld().createJoint(rjd);
//    }

    @Override
    public void keyPressed(char key, int argKeyCode) {
        switch (key) {
            case 'a':
                m_hipJoint.setMotorSpeed(-m_motorSpeed);
                break;

            case 's':
                m_hipJoint.setMotorSpeed(0.0f);
                break;

            case 'd':
                m_hipJoint.setMotorSpeed(m_motorSpeed);
                break;

            case 'm':
                m_hipJoint.enableMotor(!m_hipJoint.isMotorEnabled());
                break;
        }
    }

    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);
        addTextLine("Keys: TODO");
    }

    @Override
    public String getTestName() {
        return "Biped Hopper";
    }
}

