package fi.jakojaannos.roguelite.game.data.components;

import org.joml.Vector2d;

public class Velocity extends Vector2d {
    public Velocity() {
        super(0.0, 0.0);
    }

    public Velocity(final Vector2d source) {
        super(source.x(), source.y());
    }

    public Velocity(final double x, final double y) {
        super(x, y);
    }
}
