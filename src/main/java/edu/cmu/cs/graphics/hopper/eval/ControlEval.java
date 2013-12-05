package edu.cmu.cs.graphics.hopper.eval;

import edu.cmu.cs.graphics.hopper.control.Avatar;
import edu.cmu.cs.graphics.hopper.control.ControlPrim;
import edu.cmu.cs.graphics.hopper.problems.ProblemDefinition;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;

/** Tests the effectiveness of a control prim for an avatar when applied to a particular problem instance.
 * This is one of the primary building blocks of a control exploration. */
 public abstract class ControlEval {
    //Getters here mostly just to indicate data which should be attached to the eval object
    public abstract Avatar getAvatar();
    public abstract ProblemDefinition getProblem();
    public abstract ControlPrim getControlPrim();

    float currPrimTime;

    //Simulation params
    World world;

    public void init() {
        Vec2 gravity = new Vec2(0, -10f);
        world = new World(gravity);

        getAvatar().init(world);
        getProblem().init(world);
    }

    public void run() {
        //Timestep until end of control primitive
        float updateHz = 60.0f;
        int posIters = 3;
        int velIters = 8;

        currPrimTime = 0.0f;
        float dt = 1/updateHz;
        while (!getControlPrim().isCompleted(currPrimTime)) {
            update(dt, posIters, velIters);
            currPrimTime += dt;
        }
        finishEvaluation();
    }

    /** Runs a single timestep of the simulation & updates fitness evaluation
     * This is used by the run() eval procedure, but also may be called manually by outside classes
     * to aid in debugging of an evaluation (eg: running single eval steps from a GUI)
     * @param dt
     */
    public void update(float dt, int velIters, int posIters) {
        getAvatar().update(dt);
        world.step(dt, velIters, posIters);
        updateEvaluation();
    }

    /** Returns current (or, if completed, final) fitness evaluation of control on problem */
    public abstract float getEvaluation();

    /** Incrementally modifies evaluation of controller fitness for current timestep */
    protected abstract void updateEvaluation();

    /** Runs any final evaluation of controller fitness (this is called at the end of simulation run) */
    protected abstract void finishEvaluation();
}
