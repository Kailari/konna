package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

/*
 * Baruuk's weapon of choice.
 */
public class NoWeapon implements Weapon {
    private final TriggerMechanism triggerMechanism;
    private final FiringMechanism firingMechanism;

    public NoWeapon() {
        this.triggerMechanism = new NoTriggerMechanism();
        this.firingMechanism = new NoFiringMechanism();
    }

    @Override
    public boolean canFire(
            final EntityManager entityManager,
            final Entity owner,
            final TimeManager timeManager,
            final AttackAbility attackAbility,
            final WeaponStats weaponStats
    ) {
        return false;
    }

    @Override
    public TriggerMechanism getTrigger() {
        return this.triggerMechanism;
    }

    @Override
    public FiringMechanism getFiringMechanism() {
        return this.firingMechanism;
    }

    private static class NoTriggerMechanism implements TriggerMechanism {

        @Override
        public void pull(
                final EntityManager entityManager,
                final Entity owner,
                final TimeManager timeManager,
                final AttackAbility attackAbility,
                final WeaponStats weaponStats
        ) {
        }

        @Override
        public void release(
                final EntityManager entityManager,
                final Entity owner,
                final TimeManager timeManager,
                final AttackAbility attackAbility,
                final WeaponStats weaponStats
        ) {
        }

        @Override
        public boolean shouldTrigger(
                final EntityManager entityManager,
                final Entity owner,
                final TimeManager timeManager,
                final AttackAbility attackAbility,
                final WeaponStats weaponStats
        ) {
            return false;
        }

        @Override
        public void equip(final EntityManager entityManager, final Entity owner) {
        }

        @Override
        public void unequip(final EntityManager entityManager, final Entity owner) {
        }
    }

    private static class NoFiringMechanism implements FiringMechanism {

        @Override
        public boolean isReadyToFire(
                final TimeManager timeManager, final AttackAbility attackAbility, final WeaponStats weaponStats
        ) {
            return false;
        }

        @Override
        public void fire(
                final EntityManager entityManager,
                final WeaponStats weaponStats,
                final TimeManager timeManager,
                final AttackAbility attackAbility,
                final Entity shooter
        ) {
        }

        @Override
        public void equip(final EntityManager entityManager, final Entity owner) {

        }

        @Override
        public void unequip(final EntityManager entityManager, final Entity owner) {

        }
    }
}
