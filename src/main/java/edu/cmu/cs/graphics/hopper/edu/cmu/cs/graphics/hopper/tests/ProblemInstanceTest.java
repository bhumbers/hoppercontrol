package edu.cmu.cs.graphics.hopper.edu.cmu.cs.graphics.hopper.tests;

import com.jogamp.newt.event.KeyEvent;
import com.thoughtworks.xstream.XStream;
import edu.cmu.cs.graphics.hopper.VecUtils;
import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.BipedHopper;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import edu.cmu.cs.graphics.hopper.eval.Evaluator;
import edu.cmu.cs.graphics.hopper.problems.ProblemInstance;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.callbacks.DestructionListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;


/** Used to interactively run ProblemInstances (as of 12.5.2013, still has some biped-specific hooks in it...)
 * Note that we override & may break some stuff (like world init and saving/loading) from the TestbedTest parent class,
 * so be wary of treating this like any other type of JBox2D TestbedTest.
 * The reason for subclassing TestbedTest is mainly for convenience & time constraints: there's a lot
 * of nice GUI & interactive stuff there that I'd rather not duplicate & disentangle from less useful bits
 * just now. -bh, 12.5.2013
 */
public class ProblemInstanceTest extends TestbedTest {
    static DecimalFormat numFormat = new DecimalFormat( "#,###,###,##0.000" );

    XStream xstream;

//    float obsW = 1.0f; float obsH = 1.0f; //obstacle width, height *applied on next world init*

    ProblemInstance problem;

    //Current index for sampled worlds
    int worldSampleIdx;

    //Problem that will be swapped in at next update (required for thread safety)
    ProblemInstance nextProblem;

    boolean m_followAvatar;
    boolean m_drawAvatarDebugInfo;

    public ProblemInstanceTest() {
        super();
        xstream = new XStream();
        xstream.alias("CtrlProvider", ControlProvider.class);
        xstream.alias("BipedCtrl", BipedHopperControl.class);
        xstream.omitField(ControlProvider.class, "currControlIdx");

        m_followAvatar = true;
        m_drawAvatarDebugInfo = true;
    }

    public synchronized void setProblem(ProblemInstance problem) {
        //Note: Synchronized to prevent swapping problem out while sim steps are ongoing

        this.problem = problem;

        worldSampleIdx = 0;

        //Update the model's params to match the problem, if able
        //(we may not have a model set yet)
        setModelSettingsForProblem(problem);
    }

    private void setModelSettingsForProblem(ProblemInstance problem) {
        if (model != null && problem != null) {
            model.getSettings().getSetting(TestbedSettings.Hz).value = problem.updateHz;
            model.getSettings().getSetting(TestbedSettings.PositionIterations).value = problem.posIters;
            model.getSettings().getSetting(TestbedSettings.VelocityIterations).value = problem.velIters;
            model.getSettings().getSetting(TestbedSettings.AllowSleep).enabled = problem.allowSleep;
            model.getSettings().getSetting(TestbedSettings.WarmStarting).enabled = problem.warmStarting;
            model.getSettings().getSetting(TestbedSettings.SubStepping).enabled = problem.substepping;
            model.getSettings().getSetting(TestbedSettings.ContinuousCollision).enabled = problem.continuousCollision;
        }
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
    public void init(TestbedModel argModel) {
        //NOTE: This is a complete override of TestbedTest's version, as we want to allow the ProblemInstance to take on
        //several of the responsibilities previously delegated to TestbedTest (world creation, contact listening, etc.)

        model = argModel;
        destructionListener = new DestructionListener() {

            public void sayGoodbye(Fixture fixture) {}

            public void sayGoodbye(Joint joint) {
                if (mouseJoint == joint) {
                    mouseJoint = null;
                } else {
                    jointDestroyed(joint);
                }
            }
        };

        bomb = null;
        mouseJoint = null;

        //ProblemDefinition instance generates the world rather than this test itself
        if (problem != null) {
            problem.init();
            m_world = problem.getWorld();
            //Update the model's params to match the problem, if able
            setModelSettingsForProblem(problem);
        }
        else {
            m_world = null;
        }

        //Generate a dummy "grounding" body to be used for mouse joints
        // (note that this is different from the physical ground plane)
        if (m_world != null) {
            BodyDef bodyDef = new BodyDef();
            groundBody = m_world.createBody(bodyDef);
        }

        init(m_world, false);
    }

    @Override
    public void initTest(boolean argDeserialized) {
        if (argDeserialized)
            return;

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
//            case KeyEvent.VK_UP:
//                obsH += OBSTACLE_HEIGHT_INCREMENT;
//                reset();
//                break;
//            case KeyEvent.VK_DOWN:
//                obsH -= OBSTACLE_HEIGHT_INCREMENT;
//                reset();
//                break;
//            case KeyEvent.VK_RIGHT:
//                obsW += OBSTACLE_WIDTH_INCREMENT;
//                reset();
//                break;
//            case KeyEvent.VK_LEFT:
//                obsW -= OBSTACLE_WIDTH_INCREMENT;
//                reset();
//                break;
        }

        List<Body> bodies = null;
        if (problem != null)
            bodies = problem.getAvatar().getBodies();
        else
            bodies = new ArrayList<Body>();

        switch (key) {
//            //Save control sequence to disk
//            case 'g':
//                {
//                    saveControlSequence();
//                }

            //Modify target velocity
            case 'd':
                {
                    BipedHopperControl nextControl = getNextControl();
                    nextControl.targetBodyVelX += TARGET_VEL_INCREMENT_X;
                    problem.getCtrlProvider().specifyControlForIndex(nextControl, problem.getCtrlProvider().CurrControlIdx() + 1);
                    break;
                }
            case 'a':
            {
                BipedHopperControl nextControl = getNextControl();
                nextControl.targetBodyVelX -= TARGET_VEL_INCREMENT_X;
                problem.getCtrlProvider().specifyControlForIndex(nextControl, problem.getCtrlProvider().CurrControlIdx() + 1);
                break;
            }

            //Modify thrust (hop height) magnitude
            case 'w':
            {
                if (problem != null) {
                    BipedHopperControl nextControl = getNextControl();
                    nextControl.activeThrustDelta += THRUST_INCREMENT;
                    problem.getCtrlProvider().specifyControlForIndex(nextControl, problem.getCtrlProvider().CurrControlIdx() + 1);
                }
                break;
            }
            case 's':
            {
                if (problem != null) {
                    BipedHopperControl nextControl = getNextControl();
                    nextControl.activeThrustDelta -= THRUST_INCREMENT;
                    problem.getCtrlProvider().specifyControlForIndex(nextControl, problem.getCtrlProvider().CurrControlIdx() + 1);
                }
                break;
            }

            case 'p':
            {
                if (problem != null) {
                    BipedHopperControl nextControl = getNextControl();
                    nextControl.targetBodyVelXLegPlacementGain += LEG_PLACEMENT_GAIN_INCREMENT;
                    problem.getCtrlProvider().specifyControlForIndex(nextControl, problem.getCtrlProvider().CurrControlIdx() + 1);
                }
                break;
            }
            case 'o':
            {
                if (problem != null) {
                    BipedHopperControl nextControl = getNextControl();
                    nextControl.targetBodyVelXLegPlacementGain -= LEG_PLACEMENT_GAIN_INCREMENT;
                    problem.getCtrlProvider().specifyControlForIndex(nextControl, problem.getCtrlProvider().CurrControlIdx() + 1);
                }
                break;
            }

            //Clear velocities
            case 'v':
                for (Body b : bodies)  {
                    b.setAngularVelocity(0);
                    b.setLinearVelocity(new Vec2(0,0));
                }
                break;
            //Add positive vels
            case 'b':
                for (Body b : bodies)  {
                    b.getLinearVelocity().addLocal(VEL_INCREMENT_X, 0);
                }
                break;
            //Add negative vel
            case 'c':
                for (Body b : bodies)  {
                    b.getLinearVelocity().addLocal(-VEL_INCREMENT_X, 0);
                }
                break;
//            //Add negative rot vel to chassis
//            case 'x':
//                for (Body b : bodies)  {
//                    b.setAngularVelocity(b.getAngularVelocity() - ANG_VEL_INCREMENT);
//                }
//                break;
//            //Add positive rot vel to chassis
//            case 'n':
//                for (Body b : bodies)  {
//                    b.setAngularVelocity(b.getAngularVelocity() + ANG_VEL_INCREMENT);
//                }
//                break;

            //Toggle avatar-following for camera
            case 'z':
                m_followAvatar = !m_followAvatar;
                break;

            //Toggle avatar debug info drawing
            case 'x':
                m_drawAvatarDebugInfo = !m_drawAvatarDebugInfo;
                break;

            //Recording setup
            case 'g':

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

            //Scrub through saved states (pretty hacky for now)
            case '.':  {
                    worldSampleIdx++;
                    World sampledWorld = problem.getWorldSample(worldSampleIdx);
                    if (sampledWorld != null) {
                        m_world = sampledWorld;
                        init(m_world, false);
                    }
                    break;
            }
            case ',':   {
                worldSampleIdx--;
                World sampledWorld = problem.getWorldSample(worldSampleIdx);
                if (sampledWorld != null) {
                    m_world = sampledWorld;
                    init(m_world, false);
                }
                break;
            }
        }
    }

    @Override
    public synchronized void step(TestbedSettings settings) {
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

        if (m_world != null) {
            m_world.setAllowSleep(settings.getSetting(TestbedSettings.AllowSleep).enabled);
            m_world.setWarmStarting(settings.getSetting(TestbedSettings.WarmStarting).enabled);
            m_world.setSubStepping(settings.getSetting(TestbedSettings.SubStepping).enabled);
            m_world.setContinuousPhysics(settings.getSetting(TestbedSettings.ContinuousCollision).enabled);
        }

        pointCount = 0;

        //TEST: Try to match sim-time to real-time by running enough updates to fill the model's update rate
        int stepsToRun = 0;
        if (timeStep > 0)
            stepsToRun = (int)Math.max(1, (1/model.getTargetFps()) / timeStep);

        //Problem stepping when necessary
        if (problem != null) {
            for (int i = 0; i < stepsToRun; i++) {
                problem.update(timeStep, settings.getSetting(TestbedSettings.VelocityIterations).value,
                        settings.getSetting(TestbedSettings.PositionIterations).value);
            }
        }

        //Standard drawing (always happens, even without a problem)
        updateDrawing(settings);

        //If we've got a problem running, show info about it
        if (problem != null) {
            //Camera update
            if (m_followAvatar && problem.getAvatar() != null) {
                setCamera(problem.getAvatar().getMainBody().getPosition());
            }

            addTextLine("Runtime: " + numFormat.format(problem.getSimTime()));

            List<String> debugTextLines = new ArrayList<String>();
            List<Color3f> debugTextColors = new ArrayList<Color3f>();
            if (problem != null) {
                if (problem.getAvatar() != null)
                    problem.getAvatar().appendDebugTextLines(debugTextLines, debugTextColors);
                addTextLines(debugTextLines, debugTextColors);
                addTextLine("");
                debugTextLines.clear(); debugTextColors.clear();
                if (problem.getEvaluator() != null)
                    problem.getEvaluator().appendDebugTextLines(debugTextLines, debugTextColors);
                addTextLines(debugTextLines, debugTextColors);
                addTextLine("");
            }

            Evaluator.Status evalStatus = problem.getStatus();
            Color3f evalColor = new Color3f();
            switch (evalStatus) {
                case SUCCESS: evalColor.set(0,1,0); break;
                case FAILURE: evalColor.set(1,0.5f,0.5f); break;
                case RUNNING: evalColor.set(1,1,1); break;
            }
            addTextLine("Evaluation status: " + evalStatus.toString());//, evalColor);

            addTextLine("Sample replay: " + worldSampleIdx + "/" + problem.getNumWorldSamples());

            DebugDraw dd = getModel().getDebugDraw();
            if (m_drawAvatarDebugInfo) {
                if (problem.getAvatar() != null) {
                    Avatar avatar = problem.getAvatar();
                    avatar.drawDebugInfo(dd);
                }
            }
        }
        //Otherwise, indicate our waiting status
        else {
            addTextLine("");
            addTextLine("Waiting for a problem to arrive...");
        }
    }

    protected void addTextLines(List<String> argTextLines, List<Color3f> argColors) {
        for (int i = 0; i < argTextLines.size(); i++)
            addTextLine(argTextLines.get(i));//, argColors.get(i));
    }

    @Override
    public String getTestName() {
        return "User Oracle";
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {
        super.preSolve(contact, oldManifold);

        //Delegate to problem
        if (problem != null)
            problem.preSolve(contact, oldManifold);
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {
        super.postSolve(contact, impulse);

        //Delegate to problem
        if (problem != null)
            problem.postSolve(contact, impulse);
    }

    @Override
    public void beginContact(Contact contact) {
        super.beginContact(contact);

        //Delegate to problem
        if (problem != null)
            problem.beginContact(contact);
    }

    @Override
    public void endContact(Contact contact) {
        super.endContact(contact);

        //Delegate to problem
        if (problem != null)
            problem.endContact(contact);
    }

    protected BipedHopperControl getNextControl() {
        ControlProvider provider = problem.getCtrlProvider();

        int nextControlIdx = provider.CurrControlIdx() + 1;
        BipedHopperControl nextControl = null;
        //Create a new control if we're at or beyond the end of the prim's sequence
        //The new control by default takes on prior control step's values
        if (nextControlIdx >= provider.NumControls())
            nextControl = (BipedHopperControl)provider.getControlAtIdx(provider.NumControls() - 1).duplicate();
        //Otherwise, we'll modify the existing control object at this step
        else
            nextControl = (BipedHopperControl)provider.getControlAtIdx(nextControlIdx);
        return nextControl;
    }

//    private void saveControlSequence() {
//        String fileName = "blah.csq";
//        String sequenceXML = xstream.toXML(provider);
//        log.info("Saved control sequence to file: " + fileName);
//
//        Writer writer = null;
//        try {
//            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName), "utf-8"));
//            writer.write(sequenceXML);
//        } catch (IOException ex) {
//            log.error("Error writing control sequence to file: " + fileName + "; " + ex.getStackTrace().toString());
//        } finally {
//            try {
//                writer.close();
//            } catch (Exception ex) {
//                log.warn("Exception while closing a control sequence file: " + fileName + "; " + ex.getStackTrace().toString());
//            }
//        }
//    }
}

