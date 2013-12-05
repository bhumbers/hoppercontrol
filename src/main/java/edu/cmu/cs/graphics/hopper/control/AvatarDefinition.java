package edu.cmu.cs.graphics.hopper.control;

/** A compact definition of an avatar which may be instantiated for simulation usage */
public abstract class AvatarDefinition {
    /** Creates a single new Avatar which is suitable for use in one simulated world */
    public abstract Avatar create();
}
