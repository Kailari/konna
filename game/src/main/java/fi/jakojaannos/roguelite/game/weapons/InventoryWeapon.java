package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class InventoryWeapon<
        M extends Weapon.MagazineHandler<MS>,
        T extends Weapon.TriggerMechanism<TS>,
        F extends Weapon.FiringMechanism<FS>,
        MS, TS, FS> {
    private final Weapon<M, T, F, MS, TS, FS> weapon;
    private final WeaponStats stats;
    private final WeaponState<MS, TS, FS> state;

    public InventoryWeapon(final Weapon<M, T, F, MS, TS, FS> weapon, final WeaponStats stats) {
        this.weapon = weapon;
        this.stats = stats;
        this.state = new WeaponState<>(weapon.getMagazineHandler().createState(),
                                       weapon.getTrigger().createTriggerState(),
                                       weapon.getFiringMechanism().createState());
    }

    public Weapon<M, T, F, MS, TS, FS> getWeapon() {
        return this.weapon;
    }

    public WeaponStats getStats() {
        return this.stats;
    }

    public WeaponState<MS, TS, FS> getState() {
        return this.state;
    }

}
