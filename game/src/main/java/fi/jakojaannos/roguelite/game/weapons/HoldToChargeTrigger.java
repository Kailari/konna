package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class HoldToChargeTrigger implements Weapon.TriggerMechanism<ChargedTriggerState> {

    private final ChargedTriggerState charge;

    public HoldToChargeTrigger(final ChargedTriggerState state) {
        this.charge = state;
    }

    @Override
    public ChargedTriggerState createState(final WeaponStats stats) {
        return this.charge;
    }

    @Override
    public void pull(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final ChargedTriggerState state,
            final WeaponStats stats
    ) {
        if (!state.isTriggerReadyToCharge(timeManager, stats)) return;

        state.setCharging(true);
        state.setTriggerReadyToFire(false);
        state.setHasFired(false);
        state.setChargeStartTimestamp(timeManager.getCurrentGameTime());
    }

    @Override
    public void release(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final ChargedTriggerState state
    ) {
        if (!state.isCharging()) return;

        state.setCharging(false);
        state.setTriggerReadyToFire(true);
        state.setChargeEndTimestamp(timeManager.getCurrentGameTime());
    }

    @Override
    public boolean shouldTrigger(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final ChargedTriggerState state
    ) {
        return state.isTriggerReadyToFire() && !state.hasFired();
    }
}
