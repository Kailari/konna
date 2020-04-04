package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class InventoryWeapon<MS, TS, FS> {
    private final Weapon<MS, TS, FS> weapon;
    private final WeaponStats stats;
    private final WeaponState<MS, TS, FS> state;

    public InventoryWeapon(final Weapon<MS, TS, FS> weapon, final WeaponStats stats) {
        this.weapon = weapon;
        this.stats = stats;
        this.state = new WeaponState<>(weapon.getMagazineHandler().createState(stats),
                                       weapon.getTrigger().createState(stats),
                                       weapon.getFiringMechanism().createState(stats));
    }

    public Weapon<MS, TS, FS> getWeapon() {
        return this.weapon;
    }

    public WeaponState<MS, TS, FS> getState() {
        return this.state;
    }

    public void pullTrigger(
            final EntityManager entityManager,
            final Entity entity,
            final Time timeManager
    ) {
        this.weapon.getTrigger().pull(entityManager, entity, timeManager, this.state.getTrigger());
    }

    public void releaseTrigger(
            final EntityManager entityManager,
            final Entity entity,
            final Time timeManager
    ) {
        this.weapon.getTrigger().release(entityManager, entity, timeManager, this.state.getTrigger());
    }

    public void fireIfReady(
            final EntityManager entityManager,
            final Entity entity,
            final Time timeManager,
            final AttackAbility attackAbility
    ) {
        this.weapon.fireIfReady(entityManager, entity, timeManager, this.state, this.stats, attackAbility);
    }

    public void reload(final TimeManager timeManager) {
        this.weapon.getMagazineHandler().reload(this.state.getMagazine(), this.stats, timeManager);
    }
}
