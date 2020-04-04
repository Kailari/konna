package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class SingleShotTrigger implements Weapon.TriggerMechanism<SingleShotTrigger.SingleShotTriggerState> {
    @Override
    public SingleShotTriggerState createState(final WeaponStats stats) {
        return new SingleShotTriggerState();
    }

    @Override
    public void pull(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final SingleShotTriggerState state
    ) {
        state.triggerDown = true;
    }

    @Override
    public void release(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final SingleShotTriggerState state
    ) {
        state.triggerDown = false;
        state.shotFired = false;
    }

    @Override
    public boolean shouldTrigger(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final SingleShotTriggerState state
    ) {
        if (state.triggerDown && !state.shotFired) {
            // TODO: is this a problem? TriggerMechanism assumes that weapon fires a shot
            state.shotFired = true;
            return true;
        }
        return false;
    }

    public static class SingleShotTriggerState {
        public boolean triggerDown;
        public boolean shotFired;
    }
}
