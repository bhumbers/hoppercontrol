package edu.cmu.cs.graphics.hopper.control;

/**
 * Created with IntelliJ IDEA.
 * User: bhumbers
 * Date: 11/26/13
 * Time: 10:43 AM
 * To change this template use File | Settings | File Templates.
 */
public class WormControl extends Control {
    public float[] targetLinkAngles;

    public WormControl(int numLinks) {
        targetLinkAngles = new float[numLinks];
    }

    @Override
    public Control duplicate() {
        int numLinks = targetLinkAngles.length;
        WormControl copy = new WormControl(numLinks);
        System.arraycopy(this.targetLinkAngles, 0, copy.targetLinkAngles, 0, numLinks);
        return copy;
    }
}
