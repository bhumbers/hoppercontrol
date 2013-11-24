package edu.cmu.cs.graphics.hopper.control;

import edu.cmu.cs.graphics.hopper.control.Control;

/** A primitive for control sequences, with controls varying over a normalized [0, 1] interval */
public abstract class ControlPrim {

    /** Returns control which should be active at given primitive runtime
     * (in seconds; this is the the time since prim started running)*/
    public abstract Control getControl(float primTime);

}
