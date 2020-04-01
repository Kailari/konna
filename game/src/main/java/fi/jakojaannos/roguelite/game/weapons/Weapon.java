package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public interface Weapon {
    TriggerMechanism getTrigger();

    FiringMechanism getFiringMechanism();

    default void fireIfReady(
            final EntityManager entityManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats,
            final TimeManager timeManager,
            final Entity entity
    ) {
        if (canFire(entityManager, entity, timeManager, attackAbility, weaponStats)) {
            getFiringMechanism().fire(entityManager, weaponStats, timeManager, attackAbility, entity);
        }
    }

    default boolean canFire(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        return getTrigger().shouldTrigger(entityManager, owner, timeManager, attackAbility, weaponStats)
                && getFiringMechanism().isReadyToFire(timeManager, attackAbility, weaponStats);
    }

    default void equip(final EntityManager entityManager, final Entity owner) {
        getTrigger().equip(entityManager, owner);
        getFiringMechanism().equip(entityManager, owner);
    }

    default void unequip(final EntityManager entityManager, final Entity owner) {
        getTrigger().unequip(entityManager, owner);
        getFiringMechanism().unequip(entityManager, owner);
    }

    interface TriggerMechanism {
        void pull(
                EntityManager entityManager,
                Entity owner,
                TimeManager timeManager,
                AttackAbility attackAbility,
                WeaponStats weaponStats
        );

        void release(
                EntityManager entityManager,
                Entity owner,
                TimeManager timeManager,
                AttackAbility attackAbility,
                WeaponStats weaponStats
        );

        boolean shouldTrigger(
                EntityManager entityManager,
                Entity owner,
                TimeManager timeManager,
                AttackAbility attackAbility,
                WeaponStats weaponStats
        );

        void equip(EntityManager entityManager, Entity owner);

        void unequip(EntityManager entityManager, Entity owner);
    }

    interface FiringMechanism {
        boolean isReadyToFire(TimeManager timeManager, AttackAbility attackAbility, WeaponStats weaponStats);

        void fire(
                EntityManager entityManager,
                WeaponStats weaponStats,
                TimeManager timeManager,
                AttackAbility attackAbility,
                Entity shooter
        );

        void equip(EntityManager entityManager, Entity owner);

        void unequip(EntityManager entityManager, Entity owner);
    }
}
