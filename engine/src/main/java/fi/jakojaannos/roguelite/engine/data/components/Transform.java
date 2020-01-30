package fi.jakojaannos.roguelite.engine.data.components;

import lombok.NoArgsConstructor;
import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@NoArgsConstructor
public final class Transform implements Component {
    public Vector2d position = new Vector2d();
    public double rotation = 0.0;

    public Transform(final double x, final double y) {
        this.position.set(x, y);
    }

    public Transform(final Transform source) {
        set(source);
    }

    public void set(final Transform source) {
        this.position.set(source.position);
        this.rotation = source.rotation;
    }
}
