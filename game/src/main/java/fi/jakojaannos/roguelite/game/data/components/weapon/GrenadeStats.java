package fi.jakojaannos.roguelite.game.data.components.weapon;

public class GrenadeStats {
    public double explosionDamage;
    public double explosionPushForce;
    public double explosionRadiusSquared;
    public long fuseTime;

    private GrenadeStats(
            final double explosionDamage,
            final double explosionPushForce,
            final double explosionRadiusSquared,
            final long fuseTime
    ) {
        this.explosionDamage = explosionDamage;
        this.explosionPushForce = explosionPushForce;
        this.explosionRadiusSquared = explosionRadiusSquared;
        this.fuseTime = fuseTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public double explosionDamage = 3;
        public double explosionPushForce = 30;
        public double explosionRadiusSquared = 2.5;
        public long fuseTime = 80;

        private Builder() {
        }

        public Builder explosionDamage(final double explosionDamage) {
            this.explosionDamage = explosionDamage;
            return this;
        }

        public Builder explosionPushForce(final double explosionPushForce) {
            this.explosionPushForce = explosionPushForce;
            return this;
        }

        public Builder explosionRadiusSquared(final double explosionRadiusSquared) {
            this.explosionRadiusSquared = explosionRadiusSquared;
            return this;
        }

        public Builder fuseTime(final long fuseTime) {
            this.fuseTime = fuseTime;
            return this;
        }

        public GrenadeStats build() {
            return new GrenadeStats(this.explosionDamage, this.explosionPushForce, this.explosionRadiusSquared, this.fuseTime);
        }
    }
}
