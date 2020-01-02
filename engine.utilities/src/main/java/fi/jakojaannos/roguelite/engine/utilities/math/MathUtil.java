package fi.jakojaannos.roguelite.engine.utilities.math;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MathUtil {
    public double clamp(double value, double min, double max) {
        return Math.min(Math.max(value, min), max);
    }
}
