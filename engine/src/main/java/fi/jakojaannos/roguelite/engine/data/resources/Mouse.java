package fi.jakojaannos.roguelite.engine.data.resources;

import org.joml.Vector2d;

public class Mouse {
    public final Vector2d position = new Vector2d(-999.0, -999.0);
    private final Vector2d tmpPosition = new Vector2d();
    public boolean clicked;

    public final Vector2d calculateCursorPositionRelativeToCamera(
            final CameraProperties cameraProperties,
            final Vector2d outResult
    ) {
        return cameraProperties.offsetByCameraPosition(tmpPosition.set(this.position)
                                                                  .mul(cameraProperties.viewportWidthInWorldUnits,
                                                                       cameraProperties.viewportHeightInWorldUnits)
                                                                  .negate(),
                                                       outResult);
    }
}
