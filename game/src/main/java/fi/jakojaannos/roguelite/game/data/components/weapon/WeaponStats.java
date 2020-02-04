package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class WeaponStats implements Component {
    public double attackRate;
    public double projectileSpeed;
    public double projectileSpeedNoise;
    public double attackSpread;
    public long projectileLifetimeInTicks;

    public WeaponStats() {
        this(2.0, 40.0, 2.5);
    }

    public WeaponStats(final double attackRate, final double projectileSpeed, final double attackSpread) {
        this(attackRate, projectileSpeed, attackSpread, projectileSpeed * 0.1, -1);
    }

    public WeaponStats(
            final double attackRate,
            final double projectileSpeed,
            final double attackSpread,
            final double projectileSpeedNoise,
            final long projectileLifetimeInTicks
    ) {
        this.attackRate = attackRate;
        this.projectileSpeed = projectileSpeed;
        this.attackSpread = attackSpread;
        this.projectileLifetimeInTicks = projectileLifetimeInTicks;
        this.projectileSpeedNoise = projectileSpeedNoise;
    }
}
