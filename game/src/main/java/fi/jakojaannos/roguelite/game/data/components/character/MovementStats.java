package fi.jakojaannos.roguelite.game.data.components.character;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.joml.Vector2d;

@NoArgsConstructor
@AllArgsConstructor
public class MovementStats implements Component {
    public double maxSpeed = 4.0;
    public double acceleration = 1.0;
    public double friction = 2.0;

    public Vector2d weaponOffset = new Vector2d(0.25, -0.5);

    public MovementStats(double maxSpeed, double acceleration, double friction) {
        this(maxSpeed, acceleration, friction, new Vector2d(0.25, -0.5));
    }
}
