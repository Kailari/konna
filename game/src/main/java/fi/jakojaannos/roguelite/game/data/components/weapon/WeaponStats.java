package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.engine.ecs.Component;

public class WeaponStats implements Component {
    public final double spread;
    public double projectileSpeed;
    public double projectileSpeedNoise;
    public double timeBetweenShots;
    public long projectileLifetimeInTicks;
    public double projectilePushForce;

    public WeaponStats() {
        this(20, 40.0, 2.5);
    }

    public WeaponStats(final long timeBetweenShots, final double projectileSpeed, final double spread) {
        this(timeBetweenShots, projectileSpeed, spread, projectileSpeed * 0.1, -1, 0.0);
    }

    public WeaponStats(
            final long timeBetweenShots,
            final double projectileSpeed,
            final double spread,
            final double projectileSpeedNoise,
            final long projectileLifetimeInTicks
    ) {
        this(timeBetweenShots, projectileSpeed, spread, projectileSpeedNoise, projectileLifetimeInTicks, 0.0);
    }

    public WeaponStats(
            final long timeBetweenShots,
            final double projectileSpeed,
            final double spread,
            final double projectileSpeedNoise,
            final long projectileLifetimeInTicks,
            final double projectilePushForce
    ) {
        this.projectileSpeed = projectileSpeed;
        this.spread = spread;
        this.timeBetweenShots = timeBetweenShots;
        this.projectileLifetimeInTicks = projectileLifetimeInTicks;
        this.projectileSpeedNoise = projectileSpeedNoise;
        this.projectilePushForce = projectilePushForce;
    }
}
