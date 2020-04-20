package fi.jakojaannos.roguelite.game.data.components.weapon;

import fi.jakojaannos.roguelite.game.data.resources.Weapons;
import fi.jakojaannos.roguelite.game.weapons.InventoryWeapon;

public class WeaponInventory {
    public InventoryWeapon[] slots;

    public WeaponInventory(final int slots) {
        this.slots = new InventoryWeapon[slots];
        for (int i = 0; i < slots; i++) {
            this.slots[i] = new InventoryWeapon(Weapons.NO_WEAPON);
        }
    }
}
