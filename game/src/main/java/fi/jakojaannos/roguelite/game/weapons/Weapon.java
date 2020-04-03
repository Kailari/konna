package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public interface Weapon<
        M extends Weapon.MagazineHandler<MS>,
        T extends Weapon.TriggerMechanism<TS>,
        F extends Weapon.FiringMechanism<FS>,
        MS extends Weapon.WeaponMagazineState,
        TS extends Weapon.WeaponTriggerState,
        FS extends Weapon.WeaponFiringState
        > {
    T getTrigger();

    F getFiringMechanism();

    M getMagazineHandler();

    default void fireIfReady(
            final EntityManager entityManager,
            final Entity entity,
            final TimeManager timeManager,
            final InventoryWeapon<M, T, F, MS, TS, FS> weapon,
            final AttackAbility attackAbility
    ) {
        if (canFire(entityManager, entity, timeManager, weapon)) {
            getFiringMechanism().fire(
                    entityManager,
                    entity,
                    timeManager,
                    weapon.getState().getFiring(),
                    weapon.getStats(),
                    attackAbility);
            // magazine.expendAmmo
        }
    }

    default boolean canFire(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final InventoryWeapon<M, T, F, MS, TS, FS> weapon
    ) {
        return getTrigger().shouldTrigger(entityManager, owner, timeManager, weapon.getState().getTrigger())
                && getFiringMechanism().isReadyToFire(timeManager, weapon.getState().getFiring(), weapon.getStats());
        // magazine.isReadyToFire..
    }

    interface MagazineHandler<TState extends WeaponMagazineState> {
        TState createState();
    }

    interface TriggerMechanism<TState extends WeaponTriggerState> {
        TState createState();

        void pull(
                EntityManager entityManager,
                Entity owner,
                TimeManager timeManager,
                TState state
        );

        void release(
                EntityManager entityManager,
                Entity owner,
                TimeManager timeManager,
                TState triggerState
        );

        boolean shouldTrigger(
                EntityManager entityManager,
                Entity owner,
                TimeManager timeManager,
                TState triggerState
        );
    }

    interface FiringMechanism<TState extends WeaponFiringState> {
        TState createState();

        boolean isReadyToFire(
                TimeManager timeManager,
                TState firingState,
                WeaponStats stats
        );

        void fire(
                EntityManager entityManager,
                Entity shooter,
                TimeManager timeManager,
                TState firingState,
                WeaponStats stats,
                AttackAbility attackAbility
        );
    }

    interface WeaponMagazineState {
    }

    interface WeaponTriggerState {
    }

    interface WeaponFiringState {
    }
}
