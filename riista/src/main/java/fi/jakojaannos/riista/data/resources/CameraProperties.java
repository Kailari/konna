package fi.jakojaannos.riista.data.resources;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3f;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;

public class CameraProperties {
    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f inverseProjection = new Matrix4f();

    private final Vector3f position = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f inverseViewMatrix = new Matrix4f();

    @Deprecated
    public double viewportWidthInWorldUnits = 1.0;
    @Deprecated
    public double viewportHeightInWorldUnits = 1.0;
    @Deprecated
    public EntityHandle cameraEntity;

    private boolean viewDirty = true;

    public Matrix4f getViewMatrix() {
        updateView();
        return this.viewMatrix;
    }

    public Matrix4f getInverseViewMatrix() {
        updateView();
        return this.inverseViewMatrix;
    }

    public Vector3f getPosition() {
        return this.position;
    }

    public void setPosition(final Vector3f position) {
        this.viewDirty = true;
        this.position.set(position);
    }

    public void setPosition(final Vector2d position) {
        this.viewDirty = true;
        this.position.set(position.x, position.y, this.position.z);
    }

    public void setRotation(final Quaternionf rotation) {
        this.viewDirty = true;
        this.rotation.set(rotation);
    }

    @Deprecated
    public CameraProperties(@Nullable final Entity cameraEntity) {
        this(cameraEntity != null ? cameraEntity.asHandle() : null);
    }

    @Deprecated
    public CameraProperties(@Nullable final EntityHandle cameraEntity) {
        this.cameraEntity = cameraEntity;
    }

    public CameraProperties() {
    }

    private void updateView() {
        if (this.viewDirty) {
            this.viewMatrix.identity()
                           .translate(this.position)
                           .rotate(this.rotation)
                           .invert();
            this.viewMatrix.invert(this.inverseViewMatrix);

            this.viewDirty = false;
        }
    }

    @Deprecated
    public Vector2d offsetByCameraPosition(
            final Vector2d position,
            final Vector2d outResult
    ) {
        final Vector2d cameraPosition = cameraPositionOrZero();
        return outResult.set(cameraPosition.x - position.x,
                             cameraPosition.y - position.y);
    }

    @Deprecated
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

    @Deprecated
    private Vector2d cameraPositionOrZero() {
        return new Vector2d(this.position.x, this.position.y);
    }
}
