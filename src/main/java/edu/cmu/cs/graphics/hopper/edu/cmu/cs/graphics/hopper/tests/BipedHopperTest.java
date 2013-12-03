package edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests;

import edu.cmu.cs.graphics.hopper.control.BipedHopper;
import edu.cmu.cs.graphics.hopper.VecUtils;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.problems.TerrainProblem;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/** Main scene setup and control update class for BipedHoppper
 * Modeled on the TheoJansenTest from JBox2D TestBed.
 */
public class BipedHopperTest extends TestbedTest {
//    private static final long CHASSIS_TAG = 1;
//    private static final long UPPER_LEG_TAG = 2;
//    private static final long LOWER_LEG_TAG = 8;
//    private static final long HIP_TAG = 16;
//    private static final long KNEE_TAG = 32;

    static DecimalFormat numFormat = new DecimalFormat( "#,###,###,##0.000" );

    BipedHopper m_hopper;

    TerrainProblem terrain;

    boolean m_followAvatar;

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

        //Only stable at shorter timesteps...
//        set

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
            groundFd.friction = 100.0f;
            groundFd.shape = shape;
            ground.createFixture(groundFd);

            shape.set(new Vec2(-50.0f, 0.0f), new Vec2(-50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);

            shape.set(new Vec2(50.0f, 0.0f), new Vec2(50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);
        }

        m_hopper = new BipedHopper();
        m_hopper.init(getWorld());

        m_followAvatar = true;

        //Terrain test
        Random r = new Random();
        r.setSeed(12345);
        int terrainLength = 100;
        float terrainDeltaX = 2.0f;
        float terrainMaxAmp = 4.0f;
        float y = 0.0f;
        List<Float> verts = new ArrayList<Float>(terrainLength);
        verts.add(0.01f);
        for (int i = 0; i < terrainLength; i++) {
            y = terrainMaxAmp*(r.nextFloat());
            if (y < 0)
                y = 0;
            verts.add(y);
        }
        terrain = new TerrainProblem(verts, terrainDeltaX);
        terrain.init(getWorld());
    }

    @Override
    public void keyPressed(char key, int argKeyCode) {
        float TARGET_VEL_INCREMENT_X = 0.5f;
        float THRUST_INCREMENT = 0.001f;
        float LEG_PLACEMENT_GAIN_INCREMENT = 0.01f;
        float VEL_INCREMENT_X = 0.1f;
        float ANG_VEL_INCREMENT = 0.1f;

        //TODO: Don't modify control vals directly; just set for next discrete update

        switch (key) {
            //Modify target velocity
            case 'd':
                ((BipedHopperControl)m_hopper.getCurrentControl()).m_targetBodyVelX += TARGET_VEL_INCREMENT_X;
                break;
            case 'a':
                ((BipedHopperControl)m_hopper.getCurrentControl()).m_targetBodyVelX -= TARGET_VEL_INCREMENT_X;
                break;

            //Modify thrust (hop height) magnitude
            case 'w':
                ((BipedHopperControl)m_hopper.getCurrentControl()).m_activeThrustDelta += THRUST_INCREMENT;
                break;
            case 's':
                ((BipedHopperControl)m_hopper.getCurrentControl()).m_activeThrustDelta -= THRUST_INCREMENT;
                break;

            case 'p':
                ((BipedHopperControl)m_hopper.getCurrentControl()).m_targetBodyVelXLegPlacementGain += LEG_PLACEMENT_GAIN_INCREMENT;
                break;
            case 'o':
                ((BipedHopperControl)m_hopper.getCurrentControl()).m_targetBodyVelXLegPlacementGain -= LEG_PLACEMENT_GAIN_INCREMENT;
                break;

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
            //Add negative vel
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

            //Toggle avatar-following for camera
            case 'z':
                m_followAvatar = !m_followAvatar;
                break;

            //Pause/play
            case 't':
                getModel().getSettings().pause = !getModel().getSettings().pause;
                break;

            //Step
            case 'y':
                getModel().getSettings().singleStep = true;
                if (!getModel().getSettings().pause)
                    getModel().getSettings().pause = true;
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
//        super.step(settings); //DISABLED... we're going to straight up replace this call...

        float hz = settings.getSetting(TestbedSettings.Hz).value;
        float timeStep = hz > 0f ? 1f / hz : 0;
        if (settings.singleStep && !settings.pause) {
            settings.pause = true;
        }

        if (settings.pause) {
            if (settings.singleStep) {
                settings.singleStep = false;
            } else {
                timeStep = 0;
            }
        }

        m_world.setAllowSleep(settings.getSetting(TestbedSettings.AllowSleep).enabled);
        m_world.setWarmStarting(settings.getSetting(TestbedSettings.WarmStarting).enabled);
        m_world.setSubStepping(settings.getSetting(TestbedSettings.SubStepping).enabled);
        m_world.setContinuousPhysics(settings.getSetting(TestbedSettings.ContinuousCollision).enabled);

        pointCount = 0;

        //TEST: Try to match sim-time to real-time by running enough updates to fill the model's update rate
        int stepsToRun = 0;
        if (timeStep > 0)
            stepsToRun = (int)Math.max(1, (1/model.getTargetFps()) / timeStep);

        for (int i = 0; i < stepsToRun; i++) {
            //Sim update
            m_world.step(timeStep, settings.getSetting(TestbedSettings.VelocityIterations).value,
                    settings.getSetting(TestbedSettings.PositionIterations).value);

            //Control update
            m_hopper.update(timeStep);
        }

        updateDrawing(settings);

        //Camera update
        if (m_followAvatar && m_hopper != null) {
            setCamera(m_hopper.getMainBody().getPosition());
        }

        if (m_hopper != null) {
            addTextLine("Control State: " + m_hopper.getControlState());
            addTextLine("Active Leg Spring Compression: " + numFormat.format(m_hopper.getActiveSpringJoint().getJointTranslation()));
            addTextLine("Body Vel X: " + numFormat.format(m_hopper.getMainBody().getLinearVelocity().x));
            addTextLine("Target Body Vel X: " + numFormat.format(((BipedHopperControl)m_hopper.getCurrentControl()).m_targetBodyVelX));
            addTextLine("Vel X Leg Gain: " + numFormat.format(((BipedHopperControl)m_hopper.getCurrentControl()).m_targetBodyVelXLegPlacementGain));
            addTextLine("Thrust offset: " + numFormat.format(((BipedHopperControl)m_hopper.getCurrentControl()).m_activeThrustDelta));
            addTextLine("Flight period: " + numFormat.format(m_hopper.m_currFlightPeriod));
            addTextLine("Stance period: " + numFormat.format(m_hopper.m_currStancePeriod));
            addTextLine("Target Spring Length: " + numFormat.format(m_hopper.m_targetThrustSpringLength[m_hopper.m_activeLegIdx]));
        }

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

