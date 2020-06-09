package fi.jakojaannos.roguelite.engine.utilities.math;

import org.joml.Vector2d;

public class CoordinateHelper {
    public static Vector2d transformCoordinate(
            final double centerX,
            final double centerY,
            final double rotation,
            final double x,
            final double y,
            final Vector2d result
    ) {
        result.set(x, y)
              .sub(centerX, centerY);
        return result.set(result.x * Math.cos(rotation) - result.y * Math.sin(rotation),
                          result.x * Math.sin(rotation) + result.y * Math.cos(rotation))
                     .add(centerX, centerY);
    }
}
