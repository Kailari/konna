package fi.jakojaannos.riista.data.resources;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector2d;
import org.joml.Vector3f;

public class CameraProperties {
    public final Matrix4f projection = new Matrix4f();
    public final Matrix4f inverseProjection = new Matrix4f();

    private final Vector3f position = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Matrix4f viewMatrix = new Matrix4f();
    private final Matrix4f inverseViewMatrix = new Matrix4f();

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
}
