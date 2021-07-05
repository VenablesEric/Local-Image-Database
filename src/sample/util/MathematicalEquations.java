package sample.util;

public class MathematicalEquations {

    public static int clampInt(int value, int min, int max)
    {
        if(value < min)
            return min;
        if(value > max)
            return max;

        return value;
    }
}
