package fi.jakojaannos.roguelite.engine.data.resources;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import org.joml.Vector2d;

public class Mouse implements Resource {
    public final Vector2d position = new Vector2d(-999.0, -999.0);
    public boolean clicked;

    private final Vector2d tmpPosition = new Vector2d();

    public final Vector2d calculateCursorPositionRelativeToCamera(
            final EntityManager entityManager,
            final CameraProperties cameraProperties,
            final Vector2d outResult
    ) {
        return cameraProperties.offsetByCameraPosition(tmpPosition.set(this.position)
                                                                  .mul(cameraProperties.viewportWidthInWorldUnits,
                                                                       cameraProperties.viewportHeightInWorldUnits)
                                                                  .negate(),
                                                       entityManager,
                                                       outResult);
    }
}
