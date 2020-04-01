package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class AutomaticTriggerMechanism implements Weapon.TriggerMechanism {

    @Override
    public void pull(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        final var state = entityManager.getComponentOf(owner, AutomaticTriggerMechanismState.class).orElseThrow();
        state.triggerDown = true;
    }

    @Override
    public void release(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        final var state = entityManager.getComponentOf(owner, AutomaticTriggerMechanismState.class).orElseThrow();
        state.triggerDown = false;
    }

    @Override
    public boolean shouldTrigger(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        final var state = entityManager.getComponentOf(owner, AutomaticTriggerMechanismState.class).orElseThrow();
        return state.triggerDown;
    }

    @Override
    public void equip(final EntityManager entityManager, final Entity owner) {
        entityManager.getComponentOf(owner, AutomaticTriggerMechanismState.class)
                     .ifPresentOrElse(state -> state.triggerDown = false,
                                      () -> entityManager
                                              .addComponentTo(owner, new AutomaticTriggerMechanismState()));
    }

    @Override
    public void unequip(final EntityManager entityManager, final Entity owner) {
        final var state = entityManager.getComponentOf(owner, AutomaticTriggerMechanismState.class).orElseThrow();
        state.triggerDown = false;
    }
}
