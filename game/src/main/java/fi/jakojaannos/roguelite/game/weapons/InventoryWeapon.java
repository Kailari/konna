package fi.jakojaannos.roguelite.game.weapons;

public class InventoryWeapon {
    private final ModularWeapon weapon;
    private final Weapon instance;

    public InventoryWeapon(final ModularWeapon weapon) {
        this.weapon = weapon;
        this.instance = new WeaponInstanceImpl(weapon.constructStates(), weapon.getAttributes());
    }

    public void reload(final ActionInfo info) {
        this.weapon.reload(this.instance, info);
    }

    public void pullTrigger(final ActionInfo info) {
        this.weapon.pullTrigger(this.instance, info);
    }

    public void releaseTrigger(final ActionInfo info) {
        this.weapon.releaseTrigger(this.instance, info);
    }

    public void fireIfReady(final ActionInfo info) {
        this.weapon.fire(this.instance, info);
    }

    public void equip(final ActionInfo info) {
        this.weapon.equip(this.instance, info);
    }

    public void unequip(final ActionInfo info) {
        this.weapon.unequip(this.instance, info);
    }

    public WeaponStateQuery doStateQuery(final ActionInfo info) {
        return this.weapon.weaponStateQuery(this.instance, info);
    }
}
