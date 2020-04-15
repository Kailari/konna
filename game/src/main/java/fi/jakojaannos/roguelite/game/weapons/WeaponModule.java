package fi.jakojaannos.roguelite.game.weapons;

public interface WeaponModule<TState, TAttributes> {
    TState getState(InventoryWeapon weapon);

    TAttributes getAttributes(InventoryWeapon weapon);

    void register(WeaponHooks hooks);
}
