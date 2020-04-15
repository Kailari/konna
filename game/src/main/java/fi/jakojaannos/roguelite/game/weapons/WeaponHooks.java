package fi.jakojaannos.roguelite.game.weapons;

public interface WeaponHooks {
    <TState, TAttributes> void registerReload(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, ReloadEvent> onReload,
            Phase phase
    );

    <TState, TAttributes> void registerTriggerPull(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, TriggerPullEvent> onTriggerPull,
            Phase phase
    );

    <TState, TAttributes> void registerTriggerRelease(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, TriggerReleaseEvent> onTriggerRelease,
            Phase phase
    );

    <TState, TAttributes> void registerWeaponFire(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, WeaponFireEvent> onWeaponFire,
            Phase phase
    );

    <TState, TAttributes> void registerWeaponEquip(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, WeaponEquipEvent> onEquip,
            Phase phase
    );

    <TState, TAttributes> void registerWeaponUnequip(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, WeaponUnequipEvent> onUnequip,
            Phase phase
    );
}
