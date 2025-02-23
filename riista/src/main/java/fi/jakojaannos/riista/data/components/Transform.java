package fi.jakojaannos.riista.data.components;

import org.joml.Vector2d;

public final class Transform {
    public Vector2d position = new Vector2d();
    public double rotation;

    public Transform() {
        this(0.0, 0.0);
    }

    public Transform(final Vector2d position) {
        this(position.x, position.y);
    }

    public Transform(final double x, final double y) {
        this.position.set(x, y);
        this.rotation = 0.0;
    }

    public Transform(final Transform source) {
        set(source);
    }

    public void set(final Transform source) {
        this.position.set(source.position);
        this.rotation = source.rotation;
    }
}
