package edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests;

import edu.cmu.cs.graphics.hopper.BipedHopper;
import edu.cmu.cs.graphics.hopper.VecUtils;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;


/** Main scene setup and control update class for BipedHoppper
 * Modeled on the TheoJansenTest from JBox2D TestBed.
 */
public class BipedHopperTest extends TestbedTest {
//    private static final long CHASSIS_TAG = 1;
//    private static final long UPPER_LEG_TAG = 2;
//    private static final long LOWER_LEG_TAG = 8;
//    private static final long HIP_TAG = 16;
//    private static final long KNEE_TAG = 32;

    BipedHopper m_hopper;

    @Override
    public Long getTag(Body argBody) {
//        if (argBody == m_chassis) {
//            return CHASSIS_TAG;
//        } else if (argBody == m_upperLeg) {
//            return UPPER_LEG_TAG;
//        } else if (argBody == m_lowerLeg) {
//            return LOWER_LEG_TAG;
//        }
        return null;
    }

    @Override
    public Long getTag(Joint argJoint) {
//        if (argJoint == m_hipJoint) {
//            return HIP_TAG;
//        }
        return null;
    }

    @Override
    public void processBody(Body argBody, Long argTag) {
//        if (argTag == CHASSIS_TAG) {
//            m_chassis = argBody;
//        } else if (argTag == UPPER_LEG_TAG) {
//            m_upperLeg = argBody;
//        } else if (argTag == LOWER_LEG_TAG) {
//             m_lowerLeg = argBody;
//        }
    }

    @Override
    public void processJoint(Joint argJoint, Long argTag) {
//        if (argTag == HIP_TAG) {
//            m_hipJoint = (RevoluteJoint) argJoint;
//            m_motorOn = m_hipJoint.isMotorEnabled();
//        }
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

//        getWorld().setGravity(new Vec2(0.0f,0.0f));

        // Ground
        {
            BodyDef bd = new BodyDef();
            Body ground = getWorld().createBody(bd);

            EdgeShape shape = new EdgeShape();
            shape.set(new Vec2(-50.0f, 0.0f), new Vec2(50.0f, 0.0f));
            FixtureDef groundFd = new FixtureDef();
//            groundFd.restitution = 1.0f; //assume perfectly elastic bounces
            groundFd.density = 0.0f;
            groundFd.friction = 1.0f;
            groundFd.shape = shape;
            ground.createFixture(groundFd);

            shape.set(new Vec2(-50.0f, 0.0f), new Vec2(-50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);

            shape.set(new Vec2(50.0f, 0.0f), new Vec2(50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);
        }

        m_hopper = new BipedHopper();
        m_hopper.init(getWorld());
    }

    @Override
    public void keyPressed(char key, int argKeyCode) {
        float VEL_INCREMENT_X = 0.1f;
        float ANG_VEL_INCREMENT = 0.1f;
        switch (key) {
            //Clear velocities
            case 'v':
                for (Body b : m_hopper.getBodies())  {
                    b.setAngularVelocity(0);
                    b.setLinearVelocity(new Vec2(0,0));
                }
                break;
            //Add positive vels
            case 'b':
                for (Body b : m_hopper.getBodies())  {
                    b.getLinearVelocity().addLocal(VEL_INCREMENT_X, 0);
                }
                break;
            //Clear velocities
            case 'c':
                for (Body b : m_hopper.getBodies())  {
                    b.getLinearVelocity().addLocal(-VEL_INCREMENT_X, 0);
                }
                break;
            //Add negative rot vel to chassis
            case 'x':
                for (Body b : m_hopper.getBodies())  {
                    b.setAngularVelocity(b.getAngularVelocity() - ANG_VEL_INCREMENT);
                }
                break;
            //Add positive rot vel to chassis
            case 'n':
                for (Body b : m_hopper.getBodies())  {
                    b.setAngularVelocity(b.getAngularVelocity() + ANG_VEL_INCREMENT);
                }
                break;
//            case 'a':
//                m_hipJoint.setMotorSpeed(-m_motorSpeed);
//                break;
//
//            case 's':
//                m_hipJoint.setMotorSpeed(0.0f);
//                break;
//
//            case 'd':
//                m_hipJoint.setMotorSpeed(m_motorSpeed);
//                break;
//
//            case 'm':
//                m_hipJoint.enableMotor(!m_hipJoint.isMotorEnabled());
//                break;
        }
    }

    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);

        if (m_hopper != null) {
            addTextLine("Control State: " + m_hopper.getControlState());
        }

        float hz = settings.getSetting(TestbedSettings.Hz).value;
        float timeStep = hz > 0f ? 1f / hz : 0;
        m_hopper.updateControl(timeStep);

        drawHopperDebug();
    }

    @Override
    public String getTestName() {
        return "Biped Hopper";
    }

    @Override
    public void beginContact(Contact contact) {
        super.beginContact(contact);

        //Check if this contact involves the hopper and mark the start of contact if so
        if (m_hopper != null) {
            Body hopperContactBody = m_hopper.getGroundContactBody();
            if (contact.getFixtureA().getBody() == hopperContactBody ||
                    contact.getFixtureB().getBody() == hopperContactBody) {
               m_hopper.setInContact(true);
            }
        }
    }

    @Override
    public void endContact(Contact contact) {
        super.endContact(contact);

        //Check if this contact involves the hopper and mark the end of contact if so
        if (m_hopper != null) {
            Body hopperContactBody = m_hopper.getGroundContactBody();
            if (contact.getFixtureA().getBody() == hopperContactBody ||
                    contact.getFixtureB().getBody() == hopperContactBody) {
                m_hopper.setInContact(false);
            }
        }
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        super.postSolve(contact, impulse);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        super.preSolve(contact, oldManifold);
    }

    protected void drawHopperDebug() {
        DebugDraw dd = getModel().getDebugDraw();
        if (m_hopper != null) {
            Transform bodyTransform = m_hopper.getMainBody().getTransform();

            //Get target active & idle leg directions in world coords
            Vec2 targetActiveLegDir = new Vec2(0.0f,-1.0f);
            VecUtils.rotateLocal(targetActiveLegDir, m_hopper.m_targetActiveHipAngle);
            VecUtils.rotateLocal(targetActiveLegDir, m_hopper.m_bodyPitch);
            Vec2 activeLegTorqueLine = targetActiveLegDir.mul(m_hopper.m_activeHipTorque * 0.1f);
            dd.drawSegment(bodyTransform.p, bodyTransform.p.add(targetActiveLegDir), new Color3f(1,0,0));
            dd.drawSegment(bodyTransform.p, bodyTransform.p.add(activeLegTorqueLine), new Color3f(1,0,1));

            Vec2 targetIdleLegDir = new Vec2(0.0f,-1.0f);
            VecUtils.rotateLocal(targetIdleLegDir, m_hopper.m_targetdIdleHipAngle);
            VecUtils.rotateLocal(targetIdleLegDir, m_hopper.m_bodyPitch);
            Vec2 idleLegTorqueLine = targetIdleLegDir.mul(m_hopper.m_idleHipTorque * 0.1f);
            dd.drawSegment(bodyTransform.p, bodyTransform.p.add(targetIdleLegDir), new Color3f(0,1,0));
            dd.drawSegment(bodyTransform.p, bodyTransform.p.add(idleLegTorqueLine), new Color3f(0,1,1));

        }
    }
}

