package fi.jakojaannos.roguelite.game.weapons;

import java.util.function.Supplier;

import fi.jakojaannos.roguelite.game.weapons.events.*;

/**
 * Event hooks for weapon modules. Allows weapon modules to hook into different weapon events and execute behavior
 * during them. Allows registering module states.
 */
public interface WeaponHooks {
    void registerReload(WeaponEventHandler<ReloadEvent> onReload, Phase phase);

    void registerTriggerPull(WeaponEventHandler<TriggerPullEvent> onTriggerPull, Phase phase);

    void registerTriggerRelease(WeaponEventHandler<TriggerReleaseEvent> onTriggerRelease, Phase phase);

    void registerWeaponFire(WeaponEventHandler<WeaponFireEvent> onWeaponFire, Phase phase);

    void registerWeaponEquip(WeaponEventHandler<WeaponEquipEvent> onEquip, Phase phase);

    void registerWeaponUnequip(WeaponEventHandler<WeaponUnequipEvent> onUnequip, Phase phase);

    void registerWeaponStateQuery(WeaponEventHandler<WeaponStateQuery> query, Phase phase);

    <TState> void registerStateFactory(Class<TState> stateClass, Supplier<TState> factory);
}
