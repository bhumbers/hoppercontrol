package edu.cmu.cs.graphics.hopper.control;

import java.util.ArrayList;

/** Basic control primitive which uses discrete sampled control values over its timespan */
public class SampledControlPrim extends ControlPrim {

    float timestepSize;
    ArrayList<Control> controlsByTimestep;

    /**  Creates a new control prim with given timestep size
     * timestepSize is the time that will be inserted between control samples, in seconds */
    SampledControlPrim(float timestepSize) {
        controlsByTimestep = new ArrayList<Control>();
        this.timestepSize = timestepSize;
    }

    /** Sets control which should be active at given prim runtime. This will replace
     * any existing control value for the same discrete slot as the given time
     * @param control
     * @param primTime
     */
    public void specifyControlForTime(Control control, float primTime) {
        int timestep = (int)(primTime / timestepSize);
        while (controlsByTimestep.size() <= timestep) {
            controlsByTimestep.add(null);
        }
    }

    @Override
    public Control getControl(float primTime) {
        int timestep = (int)(primTime / timestepSize);
        //Return as usual if timestep is in range for which we have control values
        if (timestep < controlsByTimestep.size())
            return controlsByTimestep.get(timestep);
        //Otherwise, clamp controls at start/end of available range if we have any
        else if (!controlsByTimestep.isEmpty()) {
            if (timestep <= 0)
                return controlsByTimestep.get(0);
            else
                return controlsByTimestep.get(controlsByTimestep.size() - 1);
        }
        //Otherwise, return no control
        else
            return null;
    }


}
