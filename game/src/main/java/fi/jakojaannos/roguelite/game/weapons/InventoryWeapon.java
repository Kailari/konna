package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class InventoryWeapon<MS, TS, FS> {
    private final Weapon<MS, TS, FS> weapon;
    private final WeaponStats stats;
    private final WeaponState<MS, TS, FS> state;

    public Weapon<MS, TS, FS> getWeapon() {
        return this.weapon;
    }

    public WeaponState<MS, TS, FS> getState() {
        return this.state;
    }

    public InventoryWeapon(final Weapon<MS, TS, FS> weapon, final WeaponStats stats) {
        this.weapon = weapon;
        this.stats = stats;
        this.state = new WeaponState<>(weapon.getMagazineHandler().createState(stats),
                                       weapon.getTrigger().createState(stats),
                                       weapon.getFiringMechanism().createState(stats));
    }

    public void pullTrigger(
            final EntityManager entityManager,
            final Entity entity,
            final TimeManager timeManager
    ) {
        this.weapon.getTrigger().pull(entityManager, entity, timeManager, this.state.getTrigger(), this.stats);
    }

    public void releaseTrigger(
            final EntityManager entityManager,
            final Entity entity,
            final TimeManager timeManager
    ) {
        this.weapon.getTrigger().release(entityManager, entity, timeManager, this.state.getTrigger());
    }

    public void fireIfReady(
            final EntityManager entityManager,
            final Entity entity,
            final TimeManager timeManager,
            final AttackAbility attackAbility
    ) {
        this.weapon.fireIfReady(entityManager, entity, timeManager, this.state, this.stats, attackAbility);
    }

    public void reload(final TimeManager timeManager) {
        this.weapon.getMagazineHandler().reload(this.state.getMagazine(), this.stats, timeManager);
    }
}
