package edu.cmu.cs.graphics.hopper.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Provides a sequence of control values as requested by some avatar
 * For example, an avatar may request new control values over a discrete time interval,
 *  or a hopper may ask for a new control at the start of each hop */
public class ControlProvider<C extends Control> {
    int currControlIdx;
    List<C> controls;

    public ControlProvider(C initControl) {
        controls = new ArrayList<C>();
        currControlIdx = 0;
        specifyControlForIndex(initControl, 0);
    }

    public ControlProvider() {
        controls = new ArrayList<C>();
        currControlIdx = 0;
    }

    public int CurrControlIdx() {return currControlIdx;}
    public int NumControls()    {return controls.size();}

    public void goToFirstControl() {
        currControlIdx = 0;
    }

    public void goToNextControl() {
//        if (currControlIdx < NumControls() - 1)
            currControlIdx++;
    }

    /** Sets a control at given index for this provider
     * If the control is beyond the size of the current list of controls, the prior final control in the list
     * will be duplicated up to the specified index in order to maintain a valid control list. */
    public void specifyControlForIndex(C control, int idx) {
        //If necessary. extend prior control up to the timestep
        while (controls.size() <= idx - 1)  {
            C priorControl = controls.get(controls.size() - 1);
            C priorControlDup = (C)priorControl.duplicate();
            controls.add(priorControlDup);
        }

        //Then append/replace the given timestep with the control
        if (idx < controls.size())
            controls.set(idx, control);
        else
            controls.add(idx, control);
    }

    /** Returns reference to current available control from this provider
     * (be aware that this is a direct ref to internal object, so be careful w/ mutations)   */
    public C getCurrControl() {
        return getControlAtIdx(currControlIdx);
    }

    /** Returns reference to controller at given index from this provider
     * (be aware that this is a direct ref to internal object, so be careful w/ mutations)   */
    public C getControlAtIdx(int idx) {
        Control control = null;

//        //Version #1: Just return null if outside available range
//        if (idx < controls.size())
//            return controls.get(idx);
//        else
//            return null;

        //Version #2: If control index outside valid range, clamp index to allowed range

        //Return as usual if index is in range for which we have control values
        if (idx < controls.size())
            return controls.get(idx);
        //Otherwise, clamp controls at start/end of available range if we have any
        else if (!controls.isEmpty()) {
            if (idx <= 0)
                return controls.get(0);
            else
                return controls.get(controls.size() - 1);
        }
        //Otherwise, return no control
        else
            return null;
    }

    public ControlProvider<C> duplicate() {
        ControlProvider<C> dup = new ControlProvider<C>(this.controls.get(0));
        dup.currControlIdx = this.currControlIdx;
        dup.controls.clear();
        for (int i = 0; i < this.controls.size(); i++)
            dup.controls.add((C)this.controls.get(i).duplicate());
        return dup;
    }

    public float[] toNumericArray() {
        float[] vals = null;

        //NOTE: We're going to assume that the numeric array output of all controls in the list are of equal length
        //Gets a bit tricky to unpack, otherwise...

        //Get vals for each control and copy to the array for the complete sequence
        int numValsPerControl = -1;
        for (int i = 0; i < this.controls.size(); i++) {
            float[] controlVals = this.controls.get(i).toNumericArray();
            if (i == 0) {
                numValsPerControl = controlVals.length;
                vals = new float[controls.size() * numValsPerControl];
            }
            System.arraycopy(controlVals, 0, vals, i*numValsPerControl, controlVals.length);
        }

        //If nothing is in the array yet, just make a null one
        if (vals == null)
            vals = new float[0];

        return vals;
    }

    /** Returns an immutable definition of this provider based on current values */
    public ControlProviderDefinition toDefinition() {
        ControlProviderDefinition controlDef = new ControlProviderDefinition(this.controls);
        return controlDef;
    }
}
