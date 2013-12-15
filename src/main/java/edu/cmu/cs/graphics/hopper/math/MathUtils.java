package edu.cmu.cs.graphics.hopper.math;

public class MathUtils {
    /** Returns sum-of-squared differences between entries of given arrays.
     * Sadness will happen if inputs are not the same length. */
    public static double deltaSqrd(double[] a, double[] b) {
        double val = 0.0f;
        for (int i = 0; i < a.length; i++)
            val += (a[i]-b[i])*(a[i]-b[i]);
        return val;
    }
}
