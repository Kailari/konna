package fi.jakojaannos.roguelite.game.weapons;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.game.data.components.weapon.WeaponStats;

public class WeaponInventory implements Component {
    private static final InventoryWeapon UNARMED = new InventoryWeapon<>(new NoWeapon(), WeaponStats.builder().build());

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
