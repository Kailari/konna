package fi.jakojaannos.roguelite.engine.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import org.joml.Vector2d;

public class Mouse implements Resource {
    public final Vector2d position = new Vector2d(-999.0, -999.0);
    public boolean clicked;

    private final Vector2d tmpPosition = new Vector2d();

    /**
     * @deprecated Use {@link CameraProperties#offsetByCameraPosition(Vector2d, EntityManager,
     * Vector2d)} instead
     */
    @Deprecated
    public final Vector2d calculateCursorPositionRelativeToCamera(
            final Vector2d cameraPosition,
            final CameraProperties camProps,
            final Vector2d outResult
    ) {
        return outResult.set(cameraPosition.x + this.position.x * camProps.viewportWidthInWorldUnits - camProps.viewportWidthInWorldUnits / 2.0,
                             cameraPosition.y + this.position.y * camProps.viewportHeightInWorldUnits - camProps.viewportHeightInWorldUnits / 2.0);
    }

    public final Vector2d calculateCursorPositionRelativeToCamera(
            final EntityManager entityManager,
            final CameraProperties cameraProperties,
            final Vector2d outResult
    ) {
        // FIXME: This might not be correct
        return cameraProperties.offsetByCameraPosition(tmpPosition.set(this.position)
                                                                  .mul(cameraProperties.viewportWidthInWorldUnits,
                                                                       cameraProperties.viewportHeightInWorldUnits),
                                                       entityManager,
                                                       outResult);
    }
}
