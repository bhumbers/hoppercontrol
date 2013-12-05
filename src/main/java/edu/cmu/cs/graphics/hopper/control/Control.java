package edu.cmu.cs.graphics.hopper.control;

/** Control values active at a particular timestep */
public abstract class Control {
    /** Creates a deep copy of this control object */
    public abstract Control duplicate();

    /** Instantiate control from a dense numeric array */
    public abstract Control fromNumericArray(float[] vals);
}
