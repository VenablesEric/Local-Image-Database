package sample.util;

/**
 * A class that contains mathematical equations.
 */
public class MathematicalEquations {

    /**
     * @param value the value that will be clamped.
     * @param min   the lowest number that {@code value} can be.
     * @param max   the highest number that {@code value} can be.
     * @return clamp {@code value} that is in-between {@code min} and {@code max}.
     */
    public static int clampInt(int value, int min, int max) {
        if (value < min)
            return min;
        if (value > max)
            return max;

        return value;
    }
}
