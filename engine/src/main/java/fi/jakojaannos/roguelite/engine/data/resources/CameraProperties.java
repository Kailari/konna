package fi.jakojaannos.roguelite.engine.data.resources;

import org.joml.Vector2d;

import java.util.Optional;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;

public class CameraProperties {
    public double viewportWidthInWorldUnits;
    public double viewportHeightInWorldUnits;

    public double targetViewportSizeInWorldUnits = 24;
    public boolean targetViewportSizeRespectiveToMinorAxis = true;

    public EntityHandle cameraEntity;

    @Deprecated
    public CameraProperties(@Nullable final Entity cameraEntity) {
        this(cameraEntity != null ? cameraEntity.asHandle() : null);
    }

    public CameraProperties(@Nullable final EntityHandle cameraEntity) {
        this.cameraEntity = cameraEntity;
    }

    public Vector2d offsetByCameraPosition(
            final Vector2d position,
            final Vector2d outResult
    ) {
        final Vector2d cameraPosition = cameraPositionOrZero();
        return outResult.set(cameraPosition.x - position.x - this.viewportWidthInWorldUnits / 2.0,
                             cameraPosition.y - position.y - this.viewportHeightInWorldUnits / 2.0);
    }

    public void calculateRelativePositionAndReMapToSize(
            final Vector2d position,
            final int targetWidth,
            final int targetHeight,
            final Vector2d outResult
    ) {
        offsetByCameraPosition(position, outResult);
        outResult.set(-outResult.x / this.viewportWidthInWorldUnits * targetWidth,
                      -outResult.y / this.viewportHeightInWorldUnits * targetHeight);
    }

    private Vector2d cameraPositionOrZero() {
        return Optional.ofNullable(this.cameraEntity)
                       .flatMap(entity -> entity.getComponent(Transform.class)
                                                .map(transform -> transform.position))
                       .orElse(new Vector2d(0.0, 0.0));
    }
}
