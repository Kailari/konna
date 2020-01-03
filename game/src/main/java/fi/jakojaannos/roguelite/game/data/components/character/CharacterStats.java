package fi.jakojaannos.roguelite.game.data.components.character;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.input.MouseInfo;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.joml.Vector2d;

@NoArgsConstructor
@AllArgsConstructor
public class CharacterStats implements Component {
    public double speed = 4.0;
    public double acceleration = 1.0;
    public double friction = 2.0;

    public Vector2d weaponOffset = new Vector2d(0.25, -0.5);

    public CharacterStats(double speed, double acceleration, double friction) {
        this(speed, acceleration, friction, new Vector2d(0.25, -0.5));
    }
}
