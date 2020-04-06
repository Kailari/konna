package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class AutomaticTriggerMechanism implements Weapon.TriggerMechanism<AutomaticTriggerState> {
    @Override
    public AutomaticTriggerState createState(final WeaponStats stats) {
        return new AutomaticTriggerState();
    }

    @Override
    public void pull(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AutomaticTriggerState state,
            final WeaponStats stats
    ) {
        state.triggerDown = true;
    }

    @Override
    public void release(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AutomaticTriggerState state
    ) {
        state.triggerDown = false;
    }

    @Override
    public boolean shouldTrigger(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AutomaticTriggerState state
    ) {
        return state.triggerDown;
    }
}
