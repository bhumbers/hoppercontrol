package edu.cmu.cs.graphics.hopper.problems;

import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.AvatarDefinition;
import edu.cmu.cs.graphics.hopper.control.BipedHopperControl;
import edu.cmu.cs.graphics.hopper.control.ControlProvider;
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

    //Serializers for state sampling
    boolean useSampling;
    List<WorldSample> worldSamples;
    PbSerializer serializer = new PbSerializer();
    PbDeserializer deserializer = new PbDeserializer();

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
    public ProblemStatus getStatus() {return status;}
    public ControlProvider getCtrlProvider() {
        if (avatar != null)
            return avatar.getControlProvider();
        return null;
    }
    public int getNumWorldSamples() {
        return worldSamples.size();
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

        //TODO: This really should be cleared here, but I'm preventing that to hack in
        // ability to review prior problem instance runs in GUI. -bh, 12.5.2013
//        worldSamples.clear();

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
            ((ControlProvider<BipedHopperControl>)avatar.getControlProvider()).getCurrControl().targetBodyVelX = INIT_VEL_X;

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

        //If sampling is enabled and enough time has passed, store a sample
        float samplingTimestep = 1.0f / 10.0f; //10 Hz
        if (useSampling &&
                (worldSamples.size() == 0 ||
                (simTime - worldSamples.get(worldSamples.size() - 1).simTime) >= samplingTimestep))
        {
            worldSamples.add(new WorldSample(simTime, serializer.serializeWorld(world).build()));
        }
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
