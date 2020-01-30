package fi.jakojaannos.roguelite.game.data.components.character;

import lombok.RequiredArgsConstructor;
import org.joml.Vector2d;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.DamageSource;

@RequiredArgsConstructor
public class CharacterAbilities implements Component {
    public final DamageSource<?> damageSource;
    public double attackTimer;
    public Vector2d attackTarget = new Vector2d();
}
