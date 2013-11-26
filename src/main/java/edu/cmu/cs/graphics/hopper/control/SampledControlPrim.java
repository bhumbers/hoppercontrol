package edu.cmu.cs.graphics.hopper.control;

import java.util.ArrayList;

/** Basic control primitive which uses discrete sampled control values over its timespan */
public class SampledControlPrim extends ControlPrim {

    float timestepSize;
    ArrayList<Control> controlsByTimestep;

    /**  Creates a new control prim with given timestep size
     * timestepSize is the time that will be inserted between control samples, in seconds */
    public SampledControlPrim(float timestepSize) {
        controlsByTimestep = new ArrayList<Control>();
        this.timestepSize = timestepSize;
    }

    @Override
    public boolean isCompleted(float primTime) {
        float endTime = timestepSize * controlsByTimestep.size();
        return (primTime >= endTime);
    }

    /** Returns discrete control timestep index for given primitive runtime */
    public int getTimestep(float primTime) {
        int timestep = (int)(primTime / timestepSize);
        return timestep;
    }

    /** Returns number of discrete control timesteps currently available in the primitive */
    public int getNumTimesteps() {
        return controlsByTimestep.size();
    }

    /** Sets control which should be active at given prim runtime. This will replace
     * any existing control value for the same discrete slot as the given time
     * @param control
     * @param primTime
     */
    public void specifyControlForTime(Control control, float primTime) {
        int timestep = getTimestep(primTime);
        specifyControlForTimeStep(control, timestep);
    }

    /** Specific to discrete controls, allows setting control for a specific timestep rather than some continuous time */
    public void specifyControlForTimeStep(Control control, int primStep) {

        //If necessary. extend prior control up to the timestep
        while (controlsByTimestep.size() <= primStep - 1)  {
            Control priorControl = controlsByTimestep.get(controlsByTimestep.size() - 1);
            Control priorControlDup = priorControl.duplicate();
            controlsByTimestep.add(priorControlDup);
        }

        //Then append/replace the given timestep with the control
        if (primStep < controlsByTimestep.size())
            controlsByTimestep.set(primStep, control);
        else
            controlsByTimestep.add(primStep, control);

    }

    @Override
    public Control getControl(float primTime) {
        int timestep = getTimestep(primTime);
        return getTimestepControl(timestep);
    }

    public Control getTimestepControl(int primStep) {
        //Return as usual if timestep is in range for which we have control values
        if (primStep < controlsByTimestep.size())
            return controlsByTimestep.get(primStep);
            //Otherwise, clamp controls at start/end of available range if we have any
        else if (!controlsByTimestep.isEmpty()) {
            if (primStep <= 0)
                return controlsByTimestep.get(0);
            else
                return controlsByTimestep.get(controlsByTimestep.size() - 1);
        }
        //Otherwise, return no control
        else
            return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < controlsByTimestep.size(); i++) {
            sb.append("Step ");
            sb.append(i);
            sb.append(": ");
            sb.append(controlsByTimestep.toString());
        }
        return sb.toString();
    }

}
