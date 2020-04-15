package fi.jakojaannos.roguelite.game.weapons;

public interface WeaponHooks {
    <TState, TAttributes> void onReload(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, ReloadEvent> onReload,
            Phase phase
    );

    <TState, TAttributes> void onTriggerPull(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, TriggerPullEvent> onTriggerPull,
            Phase phase
    );

    <TState, TAttributes> void onTriggerRelease(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, TriggerReleaseEvent> onTriggerRelease,
            Phase phase
    );

    <TState, TAttributes> void onWeaponFire(
            WeaponModule<TState, TAttributes> module,
            WeaponEventHandler<TState, TAttributes, WeaponFireEvent> onWeaponFire,
            Phase phase
    );
}
