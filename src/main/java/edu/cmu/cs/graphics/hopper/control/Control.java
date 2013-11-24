package edu.cmu.cs.graphics.hopper.control;

/** Control values at a discrete timestep */
public abstract class Control {
    /** Applies a control to its target avatar */
    public abstract void apply();
}
