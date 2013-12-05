package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** An instantiated, runnable problem created from some problem definition
 * (contains a lot of dynamic state; meant for a singe sim run & done)/.
 * (NOTE: This borrows a lot from Box2D's TestbedTest, as it's similarly responsible for "managing" a simulation run,
 * but we cut out a lot of the GUI-specific stuff so it's more amenable to auto-sims. */
public class ProblemInstance implements
        ContactListener {

    public enum ProblemStatus {
        RUNNING,
        SOLVED,
        FAILURE
    }

    protected static final Logger log = LoggerFactory.getLogger(ProblemInstance.class);

    ProblemStatus status;
    protected int stepCount;
    float simTime;

    //Static definition stuff
    final AvatarDefinition avatarDef;
    final ProblemDefinition problemDef;

    //Dynamic runtime stuff
    protected World world;
    protected Avatar avatar;

    //Simulation stepping stuff
    public int updateHz;             //determines simulation update timestep (1/updateHz)
    public int posIters;
    public int velIters;
    public boolean allowSleep;
    public boolean warmStarting;
    public boolean substepping;
    public boolean continuousCollision;


    protected ControlProvider givenCtrlProvider;

    /** Creates a new problem instance where avatar will use default control provider */
    public ProblemInstance(ProblemDefinition problemDef, AvatarDefinition avatarDef) {
        this(problemDef, avatarDef, null);
    }

    /** Creates a new problem instance where avatar will use given control provider */
    public ProblemInstance(ProblemDefinition problemDef, AvatarDefinition avatarDef, ControlProvider ctrlProvider) {
        this.problemDef = problemDef;
        this.avatarDef = avatarDef;
        this.givenCtrlProvider = ctrlProvider;
    }

    public float getSimTime() {return simTime;}
    public World getWorld() {return world;}
    public Avatar getAvatar() {return avatar;}
    public ProblemStatus getStatus() {return status;}
    public ControlProvider getCtrlProvider() {
        if (avatar != null)
            return avatar.getControlProvider();
        return null;
    }

    public void init() {
        status = ProblemStatus.RUNNING;
        simTime = 0;
        stepCount = 0;

        //NOTE: We're going to hardcode pretty high values for now (required by biped hopper)
        updateHz = 1000;
        posIters = 30;
        velIters = 50;
        allowSleep = true;
        warmStarting = true;
        substepping =  false;
        continuousCollision = true;

        Vec2 gravity = new Vec2(0, -10f);
        world = new World(gravity);

        if (avatarDef != null) {
            avatar = avatarDef.create();

            //If given a specific provider, use it
            if (givenCtrlProvider != null)
                avatar.setControlProvider(givenCtrlProvider);

            //TODO: move this to problem or avatar def... just useful to hardcode for now
            final float INIT_VEL_X = 1.0f;
            avatar.setInitState(new Vec2(-10.0f, 8.0f), new Vec2(INIT_VEL_X, 0.0f));
//            ctrlProvider.getCurrControl().targetBodyVelX = INIT_VEL_X;

            avatar.init(world);
        }
        problemDef.init(world);

        //Create basic flat ground (TODO: Move this to ProblemDefinition defs instead?)
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

        world.setContactListener(this);
    }

    public void run() {
        //Timestep through complete problem instance test
        simTime = 0.0f;
        float dt = 1.0f/updateHz;
        while (status == ProblemStatus.RUNNING) {
            update(dt, posIters, velIters);
        }
    }

    public void update(float dt, int velIters, int posIters) {
        avatar.update(dt);
        world.step(dt, velIters, posIters);
        simTime += dt;

        //TODO: Actually evaluate problem status. Solved? Failed?
        //This is probably best delegated to the problem def (eg: problemDef.getStatus(World, Avatar) or some "evaluator" class,
        //the latter allowing us to decouple a problem definition from how we evaluate if it's solved or not.
        //TESTING: Solved if we cross some dist to the right, failed if we go left (arbirtrary) or run too long
        if (simTime > 10.0f)
            status = ProblemStatus.FAILURE;
        else if (avatar.getMainBody().getPosition().x > 1.0f)
            status = ProblemStatus.SOLVED;
        else
            status = ProblemStatus.RUNNING;
    }

    @Override
    public void beginContact(Contact contact) {
        if (avatar != null) avatar.onBeginContact(contact);
    }

    @Override
    public void endContact(Contact contact) {
        if (avatar != null) avatar.onEndContact(contact);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}


}
