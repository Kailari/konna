package fi.jakojaannos.roguelite.game.data.components.weapon;

import lombok.AllArgsConstructor;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.DamageSource;

@AllArgsConstructor
public class ProjectileStats implements Component {
    public double damage;
    public DamageSource<?> damageSource;
}
