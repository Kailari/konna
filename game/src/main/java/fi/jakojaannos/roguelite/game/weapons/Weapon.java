package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public interface Weapon<MS, TS, FS> {
    TriggerMechanism<TS> getTrigger();

    Weapon.FiringMechanism<FS> getFiringMechanism();

    MagazineHandler<MS> getMagazineHandler();

    default void fireIfReady(
            final EntityManager entityManager,
            final Entity entity,
            final TimeManager timeManager,
            final WeaponState<MS, TS, FS> state,
            final WeaponStats stats,
            final AttackAbility attackAbility
    ) {
        if (canFire(entityManager, entity, timeManager, state, stats)) {
            getFiringMechanism().fire(
                    entityManager,
                    entity,
                    timeManager,
                    state.getFiring(),
                    stats,
                    attackAbility);
            getMagazineHandler().expendAmmo(state.getMagazine(), stats);
        }
    }

    default boolean canFire(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final WeaponState<MS, TS, FS> state,
            final WeaponStats stats
    ) {
        return getTrigger().shouldTrigger(entityManager, owner, timeManager, state.getTrigger())
                && getFiringMechanism().isReadyToFire(timeManager, state.getFiring(), stats)
                && getMagazineHandler().canFire(state.getMagazine(), stats, timeManager);
    }

    interface MagazineHandler<TState> {
        TState createState(WeaponStats stats);

        boolean canFire(
                TState state,
                WeaponStats stats,
                TimeManager timeManager
        );

        void expendAmmo(TState state, WeaponStats stats);

        void reload(TState state, WeaponStats stats, TimeManager timeManager);
    }

    interface TriggerMechanism<TState> {
        TState createState(WeaponStats stats);

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

    interface FiringMechanism<TState> {
        TState createState(WeaponStats stats);

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

}
