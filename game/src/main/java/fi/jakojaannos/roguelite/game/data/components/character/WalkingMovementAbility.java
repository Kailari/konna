package fi.jakojaannos.roguelite.game.data.components.character;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.ecs.Component;

@NoArgsConstructor
@AllArgsConstructor
public class WalkingMovementAbility implements Component {
    public double maxSpeed = 4.0;
    public double acceleration = 1.0;

    // FIXME: What the heck does this do here?
    public Vector2d weaponOffset = new Vector2d(0.25, -0.5);

    public WalkingMovementAbility(final double maxSpeed, final double acceleration) {
        this(maxSpeed, acceleration, new Vector2d(0.25, -0.5));
    }
}
