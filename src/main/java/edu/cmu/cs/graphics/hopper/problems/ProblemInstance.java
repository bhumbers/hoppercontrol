package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.*;
import edu.cmu.cs.graphics.hopper.eval.Evaluator;
import edu.cmu.cs.graphics.hopper.eval.EvaluatorDefinition;
import org.box2d.proto.Box2D;
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
import org.jbox2d.serialization.pb.PbDeserializer;
import org.jbox2d.serialization.pb.PbSerializer;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/** An instantiated, runnable problem created from some problem definition
 * (contains a lot of dynamic state; meant for a singe sim run & done)/.
 * (NOTE: This borrows a lot from Box2D's TestbedTest, as it's similarly responsible for "managing" a simulation run,
 * but we cut out a lot of the GUI-specific stuff so it's more amenable to auto-sims. */
public class ProblemInstance implements
        ContactListener {

    /** A sampled state of the problem's world at a particular sim time */
    protected final class WorldSample {
        final float simTime;
        final Box2D.PbWorld serializedWorld;

        WorldSample(float simTime, Box2D.PbWorld serializedWorld) {
            this.simTime = simTime;
            this.serializedWorld = serializedWorld;
        }
    }

    protected static final Logger log = LoggerFactory.getLogger(ProblemInstance.class);

    protected int stepCount;
    float simTime;

    //Static definition stuff
    final AvatarDefinition avatarDef;
    final ProblemDefinition problemDef;
    final EvaluatorDefinition evalDef;
    final ControlProviderDefinition ctrlDef;

    //Dynamic runtime stuff
    protected World world;
    protected Avatar avatar;
    protected Evaluator eval;
    protected Body groundBody;

    //Simulation stepping stuff
    public int updateHz;             //determines simulation update timestep (1/updateHz)
    public int posIters;
    public int velIters;
    public boolean allowSleep;
    public boolean warmStarting;
    public boolean substepping;
    public boolean continuousCollision;

    protected List<Contact> currContacts;

    //Serializers for state sampling
    boolean useSampling;
    List<WorldSample> worldSamples;
    PbSerializer serializer = new PbSerializer();
    PbDeserializer deserializer = new PbDeserializer();

    protected ControlProvider givenCtrlProvider;

    /** Creates a new problem instance where avatar will use default control provider */
    public ProblemInstance(ProblemDefinition problemDef, AvatarDefinition avatarDef, EvaluatorDefinition evalDef) {
        this(problemDef, avatarDef, evalDef, null);
    }

    /** Creates a new problem instance where avatar will use given control provider */
    public ProblemInstance(ProblemDefinition problemDef, AvatarDefinition avatarDef,
                           EvaluatorDefinition evalDef, ControlProviderDefinition ctrlDef) {
        this.problemDef = problemDef;
        this.avatarDef = avatarDef;
        this.evalDef = evalDef;
        this.ctrlDef = ctrlDef;

        currContacts = new ArrayList<Contact>();

        worldSamples = new ArrayList<WorldSample>();
        //By default, don't use sampling (only necessary for debugging in most cases)
        setUseSampling(false);
    }

    /** Sets whether or not world states are sampled at regular simulation timesteps.
     * Useful for debugging, but uses more memory and may slow down performance.
     * Default is off. */
    public void setUseSampling(boolean val) {
        useSampling = val;
    }

    public float getSimTime() {return simTime;}
    public World getWorld() {return world;}
    public Avatar getAvatar() {return avatar;}
    public Evaluator getEvaluator() {return eval;}
    public Evaluator.Status getStatus() {return (eval != null) ? eval.getStatus() : Evaluator.Status.RUNNING;}
    public Body getGroundBody() {return groundBody;}
    public ControlProvider getCtrlProvider() {
        if (avatar != null)
            return avatar.getControlProvider();
        return null;
    }
    public int getNumWorldSamples() {
        return worldSamples.size();
    }
    /** Returns list of contact points generated at last sim update */
    public List<Contact> getCurrentContacts() {return currContacts;}

    public void init() {
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

        //TODO: This really should be cleared here, but I'm preventing that to hack in
        // ability to review prior problem instance runs in GUI. -bh, 12.5.2013
//        worldSamples.clear();

        Vec2 gravity = new Vec2(0, -10f);
        world = new World(gravity);

        world.setAllowSleep(allowSleep);
        world.setWarmStarting(warmStarting);
        world.setSubStepping(substepping);
        world.setContinuousPhysics(continuousCollision);

        eval = evalDef.create();
        eval.init();

        if (avatarDef != null) {
            avatar = avatarDef.create();

            //If given a specific provider, use it
            if (ctrlDef != null)
                avatar.setControlProvider(ctrlDef.create());

            //TODO: move this to problem or avatar def... just useful to hardcode for now
            final float INIT_VEL_X = 2.0f;
            final Vec2 INIT_POS = new Vec2(-7.0f, 8.0f);
            avatar.setInitState(INIT_POS, new Vec2(INIT_VEL_X, 0.0f));
            ((ControlProvider<BipedHopperControl>)avatar.getControlProvider()).getCurrControl().targetBodyVelX = INIT_VEL_X;

            avatar.init(world);
        }

        problemDef.init(world);

        //Create basic flat ground (TODO: Move this to ProblemDefinition defs instead?)
        {
            BodyDef bd = new BodyDef();
            groundBody = getWorld().createBody(bd);

            float groundLength = 200.0f;

            EdgeShape shape = new EdgeShape();
            shape.set(new Vec2(-groundLength/2, 0.0f), new Vec2(groundLength/2, 0.0f));
            FixtureDef groundFd = new FixtureDef();
//            groundFd.restitution = 1.0f; //assume perfectly elastic bounces
            groundFd.density = 0.0f;
            groundFd.friction = 100.0f;
            groundFd.shape = shape;
            groundBody.createFixture(groundFd);

            shape.set(new Vec2(-groundLength/2, 0.0f), new Vec2(-groundLength/2, 10.0f));
            groundBody.createFixture(shape, 0.0f);

            shape.set(new Vec2(groundLength/2, 0.0f), new Vec2(groundLength/2, 10.0f));
            groundBody.createFixture(shape, 0.0f);
        }

        world.setContactListener(this);
    }

    public void run() {
        //Timestep through complete problem instance test
        simTime = 0.0f;
        float dt = 1.0f/updateHz;
        while (getStatus() == Evaluator.Status.RUNNING) {
            update(dt, velIters, posIters);
        }
        finish();
    }

    public void update(float dt, int velIters, int posIters) {
        currContacts.clear();

        avatar.update(dt);
        world.step(dt, velIters, posIters);
        simTime += dt;

        eval.updateEvaluation(this);

        //If sampling is enabled and enough time has passed, store a sample
        float samplingTimestep = 1.0f / 10.0f; //10 Hz
        if (useSampling &&
                (worldSamples.size() == 0 ||
                (simTime - worldSamples.get(worldSamples.size() - 1).simTime) >= samplingTimestep))
        {
            worldSamples.add(new WorldSample(simTime, serializer.serializeWorld(world).build()));
        }
    }

    /**Should be called when a simulation run is completed/exited in order
     * to update some final evaluation fitnes results, etc. */
    public void finish() {
        eval.finishEvaluation(this);
    }

    /** Returns sim World at given sampled index in sample list (if available)
     * The returned object is a deep copy of the sampled world, so do with it what you will. */
    public World getWorldSample(int sampleIdx) {
        if (sampleIdx >= 0 && sampleIdx < worldSamples.size())
           return deserializer.deserializeWorld(worldSamples.get(sampleIdx).serializedWorld);
        return null;
    }

    @Override
    public void beginContact(Contact contact) {
        currContacts.add(contact);
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
