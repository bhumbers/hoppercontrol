package edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests;

import com.jogamp.newt.event.KeyEvent;
import com.thoughtworks.xstream.XStream;
import edu.cmu.cs.graphics.hopper.control.BipedHopper;
import edu.cmu.cs.graphics.hopper.VecUtils;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.explore.Explorer;
import edu.cmu.cs.graphics.hopper.explore.ProblemSolutionEntry;
import edu.cmu.cs.graphics.hopper.io.IOUtils;
import edu.cmu.cs.graphics.hopper.problems.ObstacleProblemDefinition;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.*;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.*;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

import java.io.*;
import java.text.DecimalFormat;


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

    float simTime = 0.0f;                  //total simulation time

    BipedHopper m_hopper;
    ControlProvider<BipedHopperControl> provider;

//    TerrainProblemDefinition terrain;
//    ObstacleProblemDefinition obstacleProb;
    float obsW = 1.0f; float obsH = 1.0f; //obstacle width, height *applied on next world init*

    boolean m_followAvatar;

    public BipedHopperTest() {
        super();
    }

    @Override
    public Long getTag(Body argBody) { return null;}

    @Override
    public Long getTag(Joint argJoint) {return null;}

    @Override
    public void processBody(Body argBody, Long argTag) {}

    @Override
    public void processJoint(Joint argJoint, Long argTag) {}

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
            groundFd.friction = 100.0f;
            groundFd.shape = shape;
            ground.createFixture(groundFd);

            shape.set(new Vec2(-50.0f, 0.0f), new Vec2(-50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);

            shape.set(new Vec2(50.0f, 0.0f), new Vec2(50.0f, 10.0f));
            ground.createFixture(shape, 0.0f);
        }

        final float INIT_VEL_X = 1.0f;
        m_hopper = new BipedHopper();
        m_hopper.setInitState(new Vec2(-10.0f, 8.0f), new Vec2(INIT_VEL_X, 0.0f));
        m_hopper.init(getWorld());

        provider = new ControlProvider<BipedHopperControl>(new BipedHopperControl());
        provider.getCurrControl().targetBodyVelX = INIT_VEL_X;
        m_hopper.setControlProvider(provider);

        m_followAvatar = true;
    }

    @Override
    public void keyPressed(char key, int argKeyCode) {
        float TARGET_VEL_INCREMENT_X = 0.5f;
        float THRUST_INCREMENT = 0.1f;
        float LEG_PLACEMENT_GAIN_INCREMENT = 0.01f;
        float VEL_INCREMENT_X = 0.1f;
        float ANG_VEL_INCREMENT = 0.1f;
        float OBSTACLE_WIDTH_INCREMENT = 0.1f;
        float OBSTACLE_HEIGHT_INCREMENT = 0.1f;

        //Modify obstacle, reset

        switch (argKeyCode) {
            case KeyEvent.VK_UP:
                obsH += OBSTACLE_HEIGHT_INCREMENT;
                reset();
                break;
            case KeyEvent.VK_DOWN:
                obsH -= OBSTACLE_HEIGHT_INCREMENT;
                reset();
                break;
            case KeyEvent.VK_RIGHT:
                obsW += OBSTACLE_WIDTH_INCREMENT;
                reset();
                break;
            case KeyEvent.VK_LEFT:
                obsW -= OBSTACLE_WIDTH_INCREMENT;
                reset();
                break;
        }

        switch (key) {
            //Save control sequence to disk
            case 'g':
                {
                    saveControlSequence();
                }

            //Modify target velocity
            case 'd':
                {
                    BipedHopperControl nextControl = getNextControl();
                    nextControl.targetBodyVelX += TARGET_VEL_INCREMENT_X;
                    provider.specifyControlForIndex(nextControl, provider.CurrControlIdx() + 1);
                    break;
                }
            case 'a':
            {
                BipedHopperControl nextControl = getNextControl();
                nextControl.targetBodyVelX -= TARGET_VEL_INCREMENT_X;
                provider.specifyControlForIndex(nextControl, provider.CurrControlIdx() + 1);
                break;
            }

            //Modify thrust (hop height) magnitude
            case 'w':
            {
                BipedHopperControl nextControl = getNextControl();
                nextControl.activeThrustDelta += THRUST_INCREMENT;
                provider.specifyControlForIndex(nextControl, provider.CurrControlIdx() + 1);
                break;
            }
            case 's':
            {
                BipedHopperControl nextControl = getNextControl();
                nextControl.activeThrustDelta -= THRUST_INCREMENT;
                provider.specifyControlForIndex(nextControl, provider.CurrControlIdx() + 1);
                break;
            }

            case 'p':
            {
                BipedHopperControl nextControl = getNextControl();
                nextControl.targetBodyVelXLegPlacementGain += LEG_PLACEMENT_GAIN_INCREMENT;
                provider.specifyControlForIndex(nextControl, provider.CurrControlIdx() + 1);
                break;
            }
            case 'o':
            {
                BipedHopperControl nextControl = getNextControl();
                nextControl.targetBodyVelXLegPlacementGain -= LEG_PLACEMENT_GAIN_INCREMENT;
                provider.specifyControlForIndex(nextControl, provider.CurrControlIdx() + 1);
                break;
            }

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

            if (timeStep > 0)
                simTime += timeStep;
        }

        updateDrawing(settings);

        //Camera update
        if (m_followAvatar && m_hopper != null) {
            setCamera(m_hopper.getMainBody().getPosition());
        }

        if (m_hopper != null) {
            addTextLine("Runtime: " + numFormat.format(simTime));
            addTextLine("Control State: " + m_hopper.getControlState());
            addTextLine("Active Leg Spring Compression: " + numFormat.format(m_hopper.getActiveSpringJoint().getJointTranslation()));
            addTextLine("Body Vel X: " + numFormat.format(m_hopper.getMainBody().getLinearVelocity().x));
            addTextLine("Target Body Vel X: " + numFormat.format(((BipedHopperControl) m_hopper.getCurrentControl()).targetBodyVelX));
            addTextLine("Vel X Leg Gain: " + numFormat.format(((BipedHopperControl)m_hopper.getCurrentControl()).targetBodyVelXLegPlacementGain));
            addTextLine("Thrust offset: " + numFormat.format(((BipedHopperControl)m_hopper.getCurrentControl()).activeThrustDelta));
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
//
//        //Check if this contact involves the hopper and mark the start of contact if so
//        if (m_hopper != null) {
//            Body hopperContactBody = m_hopper.getGroundContactBody();
//            if (contact.getFixtureA().getBody() == hopperContactBody ||
//                    contact.getFixtureB().getBody() == hopperContactBody) {
//               m_hopper.setInContact(true);
//            }
//        }
    }

    @Override
    public void endContact(Contact contact) {
        super.endContact(contact);
//
//        //Check if this contact involves the hopper and mark the end of contact if so
//        if (m_hopper != null) {
//            Body hopperContactBody = m_hopper.getGroundContactBody();
//            if (contact.getFixtureA().getBody() == hopperContactBody ||
//                    contact.getFixtureB().getBody() == hopperContactBody) {
//                m_hopper.setInContact(false);
//            }
//        }
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
//            dd.drawSegment(bodyTransform.p, bodyTransform.p.add(activeLegTorqueLine), new Color3f(1,0,1));

            Vec2 targetIdleLegDir = new Vec2(0.0f,-1.0f);
            VecUtils.rotateLocal(targetIdleLegDir, m_hopper.m_targetdIdleHipAngle);
            VecUtils.rotateLocal(targetIdleLegDir, m_hopper.m_bodyPitch);
            Vec2 idleLegTorqueLine = targetIdleLegDir.mul(m_hopper.m_idleHipTorque * 0.1f);
            dd.drawSegment(bodyTransform.p, bodyTransform.p.add(targetIdleLegDir), new Color3f(0,1,0));
//            dd.drawSegment(bodyTransform.p, bodyTransform.p.add(idleLegTorqueLine), new Color3f(0,1,1));

            //Draw user input helper visuals
            final float lineEps = -0.1f;
            Vec2 controlP = bodyTransform.p.add(new Vec2(0, 2.0f));
            dd.drawSolidCircle(controlP, 0.1f, new Vec2(0,1), new Color3f(1,1,1));
            dd.drawSegment(controlP, controlP.add(new Vec2(getNextControl().targetBodyVelX, 0.0f)), new Color3f(0,1,0));
            dd.drawSegment(controlP.add(new Vec2(0,lineEps)), controlP.add(new Vec2(m_hopper.getMainBody().getLinearVelocity().x, lineEps)), new Color3f(1,1,0));
            dd.drawSegment(controlP, controlP.add(new Vec2(0.0f, getNextControl().activeThrustDelta * 200.0f)), new Color3f(0,1,1));
        }
    }

    protected BipedHopperControl getNextControl() {
        int nextControlIdx = provider.CurrControlIdx() + 1;
        BipedHopperControl nextControl = null;
        //Append a new control if currently none is specified (ie: we're at the end of the prim's sequence)
        //The new control by default takes on prior control step's values
        if (nextControlIdx >= provider.NumControls())
            nextControl = (BipedHopperControl)provider.getControlAtIdx(provider.NumControls() - 1).duplicate();
            //Otherwise, we'll modify the existing control object at this step
        else
            nextControl = (BipedHopperControl)provider.getControlAtIdx(nextControlIdx);
        return nextControl;
    }

    private void saveControlSequence() {
        String filename = "blah.csq";
        ProblemSolutionEntry entry = new ProblemSolutionEntry(new ObstacleProblemDefinition(1,2), provider.toDefinition());
        IOUtils.instance().saveProblemSolutionEntry(entry, "", filename);
        log.info("Saved control sequence to file: " + filename);
    }
}

