package fi.jakojaannos.roguelite.engine.data.resources;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import lombok.val;
import org.joml.Vector2d;

public class CameraProperties implements Resource {
    public double viewportWidthInWorldUnits;
    public double viewportHeightInWorldUnits;

    public double targetViewportSizeInWorldUnits = 24.0;
    public boolean targetViewportSizeRespectiveToMinorAxis = true;

    public Entity cameraEntity;

    public Vector2d calculatePositionRelativeToCamera(
            final Vector2d position,
            final EntityManager entityManager,
            final Vector2d outResult
    ) {
        val cameraPosition = entityManager.getComponentOf(this.cameraEntity, Transform.class)
                                          .map(transform -> transform.position)
                                          .orElse(new Vector2d(0.0, 0.0));
        return outResult.set(cameraPosition.x - position.x - this.viewportWidthInWorldUnits / 2.0,
                             cameraPosition.y - position.y - this.viewportHeightInWorldUnits / 2.0);
    }

    public Vector2d calculateRelativePositionAndReMapToSize(
            final Vector2d position,
            final EntityManager entityManager,
            final int targetWidth,
            final int targetHeight,
            final Vector2d outResult
    ) {
        calculatePositionRelativeToCamera(position, entityManager, outResult);
        return outResult.set(-outResult.x / this.viewportWidthInWorldUnits * targetWidth,
                             -outResult.y / this.viewportHeightInWorldUnits * targetHeight);
    }
}
