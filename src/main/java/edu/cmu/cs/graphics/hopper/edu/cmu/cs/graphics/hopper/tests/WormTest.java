package edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests;

import edu.cmu.cs.graphics.hopper.BipedHopper;
import edu.cmu.cs.graphics.hopper.VecUtils;
import edu.cmu.cs.graphics.hopper.control.Worm;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

import java.util.List;

/** Used for debugging Worm avatar */
public class WormTest extends TestbedTest {
    Worm avatar;

    @Override
    public Long getTag(Body argBody) {
        return null;
    }

    @Override
    public Long getTag(Joint argJoint) {
        return null;
    }

    @Override
    public void processBody(Body argBody, Long argTag) {

    }

    @Override
    public void processJoint(Joint argJoint, Long argTag) {
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

        avatar = new Worm(3);
        avatar.init(getWorld());
    }

    @Override
    public void keyPressed(char key, int argKeyCode) {
        float VEL_INCREMENT_X = 0.1f;
        float ANG_VEL_INCREMENT = 0.1f;

        float JOINT_INCREMENT = 0.1f;

        switch (key) {
            //Worm joint #1 control (TODO: only control indirectly so we discretize correctly)
            case 'q':
                {
                    Worm.WormControl control = (Worm.WormControl)avatar.getCurrentControl();
                    control.targetLinkAngles[0] += JOINT_INCREMENT;
                    break;
                }
            case 'w':
            {
                Worm.WormControl control = (Worm.WormControl)avatar.getCurrentControl();
                control.targetLinkAngles[0] -= JOINT_INCREMENT;
                break;
            }

            //Worm joint #2 control (TODO: only control indirectly so we discretize correctly)
            case 'o':
            {
                Worm.WormControl control = (Worm.WormControl)avatar.getCurrentControl();
                control.targetLinkAngles[1] += JOINT_INCREMENT;
                break;
            }
            case 'p':
            {
                Worm.WormControl control = (Worm.WormControl)avatar.getCurrentControl();
                control.targetLinkAngles[1] -= JOINT_INCREMENT;
                break;
            }


            //Clear velocities
            case 'v':
                for (Body b : avatar.getBodies())  {
                    b.setAngularVelocity(0);
                    b.setLinearVelocity(new Vec2(0,0));
                }
                break;
            //Add positive vels
            case 'b':
                for (Body b : avatar.getBodies())  {
                    b.getLinearVelocity().addLocal(VEL_INCREMENT_X, 0);
                }
                break;
            //Clear velocities
            case 'c':
                for (Body b : avatar.getBodies())  {
                    b.getLinearVelocity().addLocal(-VEL_INCREMENT_X, 0);
                }
                break;
            //Add negative rot vel to chassis
            case 'x':
                for (Body b : avatar.getBodies())  {
                    b.setAngularVelocity(b.getAngularVelocity() - ANG_VEL_INCREMENT);
                }
                break;
            //Add positive rot vel to chassis
            case 'n':
                for (Body b : avatar.getBodies())  {
                    b.setAngularVelocity(b.getAngularVelocity() + ANG_VEL_INCREMENT);
                }
                break;
        }
    }

    @Override
    public void step(TestbedSettings settings) {
        super.step(settings);

//        if (avatar != null) {
//            addTextLine("Control State: " + avatar.getControlState());
//        }

        float hz = settings.getSetting(TestbedSettings.Hz).value;
        float timeStep = hz > 0f ? 1f / hz : 0;
        avatar.update(timeStep);

        drawAvatarDebug();
    }

    @Override
    public String getTestName() {
        return "Worm Avatar";
    }

    @Override
    public void beginContact(Contact contact) {
        super.beginContact(contact);
    }

    @Override
    public void endContact(Contact contact) {
        super.endContact(contact);
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        super.postSolve(contact, impulse);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        super.preSolve(contact, oldManifold);
    }

    protected void drawAvatarDebug() {
        DebugDraw dd = getModel().getDebugDraw();

        if (avatar != null) {
            Transform bodyTransform = avatar.getMainBody().getTransform();

            //Show target pose for the worm
            Worm.WormControl control = (Worm.WormControl)avatar.getCurrentControl();
            List<Body> links = avatar.getBodies();
            List<? extends Joint> joints = avatar.getJoints();
            Vec2 jointPos = new Vec2();
            for (int i = 0; i < links.size() - 1; i++) {
                Body link = links.get(i);
                RevoluteJoint joint = (RevoluteJoint)joints.get(i);
                joint.getAnchorA(jointPos);
                Vec2 targetLinkDir = new Vec2(1.0f, 0.0f);
                VecUtils.rotateLocal(targetLinkDir, control.targetLinkAngles[i]);
                VecUtils.rotateLocal(targetLinkDir, link.getAngle());
                dd.drawSegment(jointPos, jointPos.add(targetLinkDir), new Color3f(1,0,0));
            }
        }
    }
}

