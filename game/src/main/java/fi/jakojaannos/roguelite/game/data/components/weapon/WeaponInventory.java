package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.game.data.resources.Weapons;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;
import fi.jakojaannos.roguelite.game.weapons.ModularWeapon;

public class WeaponInventory {
    public InventoryWeapon[] slots;

    public WeaponInventory(final int slots) {
        this.slots = new InventoryWeapon[slots];
        for (int i = 0; i < slots; i++) {
            this.slots[i] = new InventoryWeapon(Weapons.NO_WEAPON);
        }
    }

    public WeaponInventory(final ModularWeapon... weapons) {
        this.slots = new InventoryWeapon[weapons.length];
        for (int i = 0; i < this.slots.length; i++) {
            this.slots[i] = new InventoryWeapon(weapons[i]);
        }
    }
}
