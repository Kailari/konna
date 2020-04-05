package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class HoldToChargeTrigger implements Weapon.TriggerMechanism<HoldToChargeTriggerState> {

    private final HoldToChargeTriggerState charge;

    public HoldToChargeTrigger(final HoldToChargeTriggerState state) {
        this.charge = state;
    }

    @Override
    public HoldToChargeTriggerState createState(final WeaponStats stats) {
        return this.charge;
    }

    @Override
    public void pull(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final HoldToChargeTriggerState state,
            final WeaponStats stats
    ) {
        if (!state.isTriggerReadyToCharge(timeManager, stats)) return;

        state.isCharging = true;
        state.isTriggerReadyToFire = false;
        state.hasFired = false;
        state.chargeStartTimestamp = timeManager.getCurrentGameTime();
    }

    @Override
    public void release(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final HoldToChargeTriggerState state
    ) {
        if (!state.isCharging) return;

        state.isCharging = false;
        state.isTriggerReadyToFire = true;
        state.chargeEndTimestamp = timeManager.getCurrentGameTime();
    }

    @Override
    public boolean shouldTrigger(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final HoldToChargeTriggerState state
    ) {
        return state.isTriggerReadyToFire && !state.hasFired;
    }
}
