package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class HoldToChargeTriggerState {
    // TODO: split into two classes, for trigger and firing mechanism
    public boolean isCharging;
    public boolean hasFired;
    public boolean isTriggerReadyToFire;

    public long chargeStartTimestamp = -1000;
    public long chargeEndTimestamp = -1000;

    public long lastAttackTimestamp = -1000;

    public boolean isTriggerReadyToCharge(
            final TimeManager timeManager,
            final WeaponStats stats
    ) {
        final var timeSinceLastAttack = timeManager.getCurrentGameTime() - this.lastAttackTimestamp;
        return timeSinceLastAttack >= stats.timeBetweenShots;
    }
}
