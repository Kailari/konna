package fi.jakojaannos.roguelite.game.weapons;

public class WeaponInventory {
    private static final InventoryWeapon UNARMED = new InventoryWeapon(Weapons.NO_WEAPON, new WeaponAttributes());

    private final InventoryWeapon[] slots;

    public WeaponInventory(final int slots) {
        this.slots = new InventoryWeapon[slots];
        for (int i = 0; i < slots; i++) {
            this.slots[i] = UNARMED;
        }
    }

    public InventoryWeapon getWeaponAtSlot(final int slot) {
        return this.slots[slot];
    }

    public void equip(final int slot, final InventoryWeapon weapon) {
        this.slots[slot] = weapon;
    }
}
