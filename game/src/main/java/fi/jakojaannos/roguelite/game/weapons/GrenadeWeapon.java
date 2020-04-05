package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class GrenadeWeapon implements Weapon<
        EndlessMagazineHandler.EndlessMagazineState,
        ChargedTriggerState,
        ChargedFiringState> {

    private final EndlessMagazineHandler magazine;
    private final HoldToChargeTrigger trigger;
    private final ChargedFiringMechanism firing;

    public GrenadeWeapon() {
        this.magazine = new EndlessMagazineHandler();
        final var state = new GrenadeWeaponState();
        this.trigger = new HoldToChargeTrigger(state);
        this.firing = new ChargedFiringMechanism(state);
    }

    @Override
    public EndlessMagazineHandler getMagazineHandler() {
        return this.magazine;
    }

    @Override
    public HoldToChargeTrigger getTrigger() {
        return this.trigger;
    }

    @Override
    public ChargedFiringMechanism getFiringMechanism() {
        return this.firing;
    }

    public static class GrenadeWeaponState implements ChargedTriggerState, ChargedFiringState {
        private boolean isCharging;
        private boolean hasFired;
        private boolean isTriggerReadyToFire;
        private long lastAttackTimestamp = -1000;
        private long chargeStartTimestamp = -1000;
        private long chargeEndTimestamp = -1000;

        @Override
        public void setLastAttackTimestamp(final long timestamp) {
            this.lastAttackTimestamp = timestamp;
        }

        @Override
        public long getLastAttackTimestamp() {
            return 0;
        }

        @Override
        public boolean isTriggerReadyToCharge(final TimeManager timeManager, final WeaponStats stats) {
            final var timeSinceLastAttack = timeManager.getCurrentGameTime() - this.lastAttackTimestamp;
            return timeSinceLastAttack >= stats.timeBetweenShots;
        }

        @Override
        public void setCharging(final boolean isCharging) {
            this.isCharging = isCharging;
        }

        @Override
        public boolean isCharging() {
            return this.isCharging;
        }

        @Override
        public void setHasFired(final boolean hasFired) {
            this.hasFired = hasFired;
        }

        @Override
        public boolean hasFired() {
            return this.hasFired;
        }

        @Override
        public void setChargeStartTimestamp(final long timestamp) {
            this.chargeStartTimestamp = timestamp;
        }

        @Override
        public void setChargeEndTimestamp(final long timestamp) {
            this.chargeEndTimestamp = timestamp;
        }

        @Override
        public long getChargeTime() {
            return this.chargeEndTimestamp - this.chargeStartTimestamp;
        }

        @Override
        public void setTriggerReadyToFire(final boolean isReady) {
            this.isTriggerReadyToFire = isReady;
        }

        @Override
        public boolean isTriggerReadyToFire() {
            return this.isTriggerReadyToFire;
        }
    }
}
