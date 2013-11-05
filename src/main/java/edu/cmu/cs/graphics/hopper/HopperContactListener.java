package edu.cmu.cs.graphics.hopper;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.dynamics.contacts.Contact;

/** Used to react to all contact events in hopper scenes */
public class HopperContactListener implements ContactListener {
    public void beginContact(Contact contact) {}

    public void endContact(Contact contact) {}

    public void postSolve(Contact contact, ContactImpulse impulse) {}

    public void preSolve(Contact contact, Manifold oldManifold) {}
}
