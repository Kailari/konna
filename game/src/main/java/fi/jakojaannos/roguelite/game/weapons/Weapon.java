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
        if (canFire(timeManager, attackAbility, weaponStats)) {
            getFiringMechanism().fire(entityManager, weaponStats, timeManager, attackAbility, entity);
        }
    }

    default boolean canFire(
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        return getTrigger().shouldTrigger(timeManager, attackAbility, weaponStats)
                && getFiringMechanism().isReadyToFire(timeManager, attackAbility, weaponStats);
    }

    interface TriggerMechanism {
        void pull(
                TimeManager timeManager,
                AttackAbility attackAbility,
                WeaponStats weaponStats
        );

        void release(
                TimeManager timeManager,
                AttackAbility attackAbility,
                WeaponStats weaponStats
        );

        boolean shouldTrigger(TimeManager timeManager, AttackAbility attackAbility, WeaponStats weaponStats);
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
    }
}
