package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

import static fi.jakojaannos.roguelite.game.weapons.NoWeapon.*;

/*
 * Baruuk's weapon of choice.
 */
public class NoWeapon implements Weapon<NoState, NoState, NoState> {

    private final NoMagazineHandler magazine;
    private final NoTriggerMechanism trigger;
    private final NoFiringMechanism firing;

    public NoWeapon() {
        this.magazine = new NoMagazineHandler();
        this.trigger = new NoTriggerMechanism();
        this.firing = new NoFiringMechanism();
    }

    @Override
    public NoMagazineHandler getMagazineHandler() {
        return this.magazine;
    }

    @Override
    public NoTriggerMechanism getTrigger() {
        return this.trigger;
    }

    @Override
    public NoFiringMechanism getFiringMechanism() {
        return this.firing;
    }

    public static class NoState {
    }

    public static class NoMagazineHandler implements Weapon.MagazineHandler<NoState> {
        @Override
        public NoState createState() {
            return new NoState();
        }
    }

    public static class NoTriggerMechanism implements Weapon.TriggerMechanism<NoState> {
        @Override
        public NoState createState() {
            return new NoState();
        }

        @Override
        public void pull(
                final EntityManager entityManager,
                final Entity owner,
                final TimeManager timeManager,
                final NoState triggerState
        ) {

        }

        @Override
        public void release(
                final EntityManager entityManager,
                final Entity owner,
                final TimeManager timeManager,
                final NoState triggerState
        ) {

        }

        @Override
        public boolean shouldTrigger(
                final EntityManager entityManager,
                final Entity owner,
                final TimeManager timeManager,
                final NoState triggerState
        ) {
            return false;
        }
    }

    public static class NoFiringMechanism implements Weapon.FiringMechanism<NoState> {
        @Override
        public NoState createState() {
            return new NoState();
        }

        @Override
        public boolean isReadyToFire(
                final TimeManager timeManager,
                final NoState firingState,
                final WeaponStats stats
        ) {
            return false;
        }

        @Override
        public void fire(
                final EntityManager entityManager,
                final Entity shooter,
                final TimeManager timeManager,
                final NoState firingState,
                final WeaponStats stats,
                final AttackAbility attackAbility
        ) {
        }
    }
}
