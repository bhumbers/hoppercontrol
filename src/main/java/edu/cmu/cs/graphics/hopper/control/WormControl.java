package edu.cmu.cs.graphics.hopper.control;

import java.text.DecimalFormat;
import java.util.Arrays;

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
        int numLinks = this.targetLinkAngles.length;
        WormControl copy = new WormControl(numLinks);
        copy.fillFromNumericArray(this.toNumericArray());
        return copy;
    }

    @Override
    public void fillFromNumericArray(float[] vals) {
        int numLinks = vals.length;
        System.arraycopy(vals, 0, this.targetLinkAngles, 0, vals.length);
    }

    @Override
    public float[] toNumericArray() {
        float[] vals = new float[this.targetLinkAngles.length];
        int valIdx = 0;
        System.arraycopy(this.targetLinkAngles, 0, vals, 0, vals.length);
        return vals;
    }

    @Override
    public String toString() {
        String str = "";
        for (float targetLinkAngle : targetLinkAngles) {
            str += strFormat.format(targetLinkAngle) + " ";
        }
        return str;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WormControl that = (WormControl) o;

        if (!Arrays.equals(targetLinkAngles, that.targetLinkAngles)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(targetLinkAngles);
    }
}
