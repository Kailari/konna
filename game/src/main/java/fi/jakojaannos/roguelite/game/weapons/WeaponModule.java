package fi.jakojaannos.roguelite.game.weapons;

public interface WeaponModule<TAttributes> {
    void register(WeaponHooks hooks, TAttributes attributes);
}
