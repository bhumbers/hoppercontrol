package edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests;

import edu.cmu.cs.graphics.hopper.BipedHopper;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;
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

        // Ground
        {
            BodyDef bd = new BodyDef();
            Body ground = getWorld().createBody(bd);

            EdgeShape shape = new EdgeShape();
            shape.set(new Vec2(-50.0f, 0.0f), new Vec2(50.0f, 0.0f));
            FixtureDef groundFd = new FixtureDef();
//            groundFd.restitution = 1.0f; //assume perfectly elastic bounces
            groundFd.density = 0.0f;
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
        switch (key) {
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
}

