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
}
