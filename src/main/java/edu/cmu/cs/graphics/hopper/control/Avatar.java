package edu.cmu.cs.graphics.hopper.control;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.Joint;

import java.util.List;

/** A controllable character */
public abstract class Avatar<C extends Control> {
    /** Sets initial desired state of avatar (should be called *before* init()) */
    public abstract void setInitState(Vec2 initPos, Vec2 initVel);

    /**Initializes physical simulation components of this avatar in given world */
    public abstract void init(World world);

    /** Returns list of all bodies belonging to the avatar */
    public abstract List<Body> getBodies();

    /** Returns list of all joints belonging to the avatar */
    public abstract List<? extends Joint> getJoints();

    /** Returns body which is considered the root of this avatar */
    public abstract Body getMainBody();

    /** Sets control provider used by this avatar */
    public abstract void setControlProvider(ControlProvider<C> provider);

    /** Returns control provider in use by this avatar */
    public abstract ControlProvider<C> getControlProvider();

    public abstract C getCurrentControl();

    /**Applies any update logic (eg: control torques) for this avatar for a simulation timestep delta (in seconds) */
    public abstract void update(float dt);

    /** Returns a deep copy of the current state of this avatar as a POJO, suitable for serializing to JSON */
    public abstract Object getState();

    /** Runs any responsive logic for this avatar when a contact occurs during simulation */
    public void onBeginContact(Contact contact) {}
    public void onEndContact(Contact contact) {}

    /** Appends any useful lines of debug text (w/ associated colors) about this avatar (for GUIs, generally) to given lists */
    public void appendDebugTextLines(List<String> lines, List<Color3f> colors) {}

    /** Draws an desired debug visuals for this avatar (for GUIS, generally) using given debug draw object */
    public void drawDebugInfo(DebugDraw dd) {}
}
