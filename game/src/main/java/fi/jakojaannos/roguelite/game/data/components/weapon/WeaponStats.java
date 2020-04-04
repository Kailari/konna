package fi.jakojaannos.roguelite.game.data.components.weapon;

public class WeaponStats {
    public final double spread;
    public double projectileSpeed;
    public double projectileSpeedNoise;
    public double timeBetweenShots;
    public long projectileLifetimeInTicks;
    public double projectilePushForce;
    public int magazineCapacity;
    public long reloadTimeInTicks;

    private WeaponStats(
            final long timeBetweenShots,
            final double projectileSpeed,
            final double spread,
            final double projectileSpeedNoise,
            final long projectileLifetimeInTicks,
            final double projectilePushForce,
            final int magazineCapacity,
            final long reloadTimeInTicks
    ) {
        this.projectileSpeed = projectileSpeed;
        this.spread = spread;
        this.timeBetweenShots = timeBetweenShots;
        this.projectileLifetimeInTicks = projectileLifetimeInTicks;
        this.projectileSpeedNoise = projectileSpeedNoise;
        this.projectilePushForce = projectilePushForce;
        this.magazineCapacity = magazineCapacity;
        this.reloadTimeInTicks = reloadTimeInTicks;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private long timeBetweenShots = 20;
        private double projectileSpeed = 40.0;
        private double spread;
        private double projectileSpeedNoise;
        private long projectileLifetimeInTicks = -1;
        private double projectilePushForce;
        private int magazineCapacity = 30;
        private long reloadTimeInTicks = 0;

        public Builder timeBetweenShots(final long timeBetweenShots) {
            this.timeBetweenShots = timeBetweenShots;
            return this;
        }

        public Builder projectileSpeed(final double projectileSpeed) {
            this.projectileSpeed = projectileSpeed;
            return this;
        }

        public Builder spread(final double spread) {
            this.spread = spread;
            return this;
        }

        public Builder projectileSpeedNoise(final double projectileSpeedNoise) {
            this.projectileSpeedNoise = projectileSpeedNoise;
            return this;
        }

        public Builder projectileLifetimeInTicks(final long projectileLifetimeInTicks) {
            this.projectileLifetimeInTicks = projectileLifetimeInTicks;
            return this;
        }

        public Builder projectilePushForce(final double projectilePushForce) {
            this.projectilePushForce = projectilePushForce;
            return this;
        }

        public Builder magazineCapacity(final int magazineCapacity) {
            this.magazineCapacity = magazineCapacity;
            return this;
        }

        public Builder reloadTimeInTicks(final long reloadTimeInTicks) {
            this.reloadTimeInTicks = reloadTimeInTicks;
            return this;
        }

        public WeaponStats build() {
            return new WeaponStats(this.timeBetweenShots,
                                   this.projectileSpeed,
                                   this.spread,
                                   this.projectileSpeedNoise,
                                   this.projectileLifetimeInTicks,
                                   this.projectilePushForce,
                                   this.magazineCapacity,
                                   this.reloadTimeInTicks);
        }

    }

}
