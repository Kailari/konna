package fi.jakojaannos.roguelite.game.data.components.character;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.DamageSource;
import lombok.RequiredArgsConstructor;
import org.joml.Vector2d;

@RequiredArgsConstructor
public class CharacterAbilities implements Component {
    public double attackTimer;
    public Vector2d attackTarget = new Vector2d();
    public final DamageSource<?> damageSource;
}
