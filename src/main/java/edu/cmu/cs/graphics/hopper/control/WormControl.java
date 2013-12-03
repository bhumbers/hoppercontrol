package edu.cmu.cs.graphics.hopper.control;

import java.text.DecimalFormat;

/** Control specific to a Worm avatar */
public class WormControl extends Control {
    public float[] targetLinkAngles;

    //For debug string output
    static DecimalFormat strFormat = new DecimalFormat( "#,###,###,##0.000" );

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

    @Override
    public String toString() {
        String str = "";
        for (float targetLinkAngle : targetLinkAngles) {
            str += strFormat.format(targetLinkAngle) + " ";
        }
        return str;
    }
}
