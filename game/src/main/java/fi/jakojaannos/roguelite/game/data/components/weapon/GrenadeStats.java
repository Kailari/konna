package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.game.data.DamageSource;

public class GrenadeStats {
    public double explosionDamage;
    public double explosionPushForce;
    public double explosionRadiusSquared;
    public long fuseTime;
    public DamageSource<?> damageSource;

    private GrenadeStats(
            final double explosionDamage,
            final double explosionPushForce,
            final double explosionRadiusSquared,
            final long fuseTime,
            final DamageSource<?> damageSource
    ) {
        this.explosionDamage = explosionDamage;
        this.explosionPushForce = explosionPushForce;
        this.explosionRadiusSquared = explosionRadiusSquared;
        this.fuseTime = fuseTime;
        this.damageSource = damageSource;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        public double explosionDamage = 3;
        public double explosionPushForce = 30;
        public double explosionRadiusSquared = 2.5;
        public long fuseTime = 80;
        public DamageSource<?> damageSource = DamageSource.Generic.UNDEFINED;

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

        public Builder damageSource(final DamageSource<?> damageSource) {
            this.damageSource = damageSource;
            return this;
        }

        public GrenadeStats build() {
            return new GrenadeStats(this.explosionDamage, this.explosionPushForce, this.explosionRadiusSquared, this.fuseTime, this.damageSource);
        }
    }
}
