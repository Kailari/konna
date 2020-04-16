package fi.jakojaannos.roguelite.game.weapons;

public class InventoryWeapon {
    private final ModularWeapon weapon;
    private final WeaponState state;

    public WeaponAttributes getAttributes() {
        return this.weapon.getAttributes();
    }

    public WeaponState getState() {
        return this.state;
    }

    public InventoryWeapon(final ModularWeapon weapon) {
        this(weapon, new WeaponState());
    }

    public InventoryWeapon(final ModularWeapon weapon, final WeaponState state) {
        this.weapon = weapon;
        this.state = state;
    }

    public void reload(
            final ActionInfo info
    ) {
        this.weapon.reload(this, info);
    }

    public void pullTrigger(
            final ActionInfo info
    ) {
        this.weapon.pullTrigger(this, info);
    }

    public void releaseTrigger(
            final ActionInfo info
    ) {
        this.weapon.releaseTrigger(this, info);
    }

    public void fireIfReady(
            final ActionInfo info
    ) {
        this.weapon.fire(this, info);
    }

    public void equip(final ActionInfo info) {
        this.weapon.equip(this, info);
    }

    public void unequip(final ActionInfo info) {
        this.weapon.unequip(this, info);
    }

    public WeaponStateQuery doStateQuery(final ActionInfo info) {
        return this.weapon.weaponStateQuery(this, info);
    }
}
