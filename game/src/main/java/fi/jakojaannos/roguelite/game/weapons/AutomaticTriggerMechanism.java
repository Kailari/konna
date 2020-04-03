package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;

public class AutomaticTriggerMechanism implements Weapon.TriggerMechanism<AutomaticTriggerState> {
    @Override
    public AutomaticTriggerState createState() {
        return new AutomaticTriggerState();
    }

    @Override
    public void pull(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AutomaticTriggerState state
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
