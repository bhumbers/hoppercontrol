package edu.cmu.cs.graphics.hopper.control;

import edu.cmu.cs.graphics.hopper.control.Control;

/** A primitive for control sequences, giving control values that vary over a (usually) short time interval */
public abstract class ControlPrim {

    /** Returns control which should be active at given primitive runtime
     * (in seconds; this is the the time since prim started running)*/
    public abstract Control getControl(float primTime);

    /** Returns true if control prim has reached its end at given primitive runtime */
    public abstract boolean isCompleted(float primTime);

}
