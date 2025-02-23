package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.game.data.DamageSource;

public class ProjectileStats {
    public double damage;
    public DamageSource<?> damageSource;
    public double pushForce;

    public ProjectileStats(final double damage, final DamageSource<?> damageSource, final double pushForce) {
        this.damage = damage;
        this.damageSource = damageSource;
        this.pushForce = pushForce;
    }
}
