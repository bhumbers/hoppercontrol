package edu.cmu.cs.graphics.hopper.control;

/** Used to instantiate biped hopper */
public class BipedHopperDefinition extends AvatarDefinition {

    @Override
    public Avatar create() {
        //Pretty basic for now... we hardcode most parameters in the BipedHopper class itself
        BipedHopper hopper = new BipedHopper();
        return hopper;
    }
}
