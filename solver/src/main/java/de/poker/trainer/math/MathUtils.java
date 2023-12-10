package de.poker.trainer.math;

public class MathUtils {
    private MathUtils() {}

    public static double product(double[] values) {
        double v = 1.0;
        for (double value : values) {
            v *= value;
        }
        return v;
    }
}
