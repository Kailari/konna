package fi.jakojaannos.roguelite.game.data.components;

import lombok.NoArgsConstructor;
import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@NoArgsConstructor
public class Velocity extends Vector2d implements Component {
    public Velocity(final Vector2d source) {
        super(source.x(), source.y());
    }

    public Velocity(final double x, final double y) {
        super(x, y);
    }
}
