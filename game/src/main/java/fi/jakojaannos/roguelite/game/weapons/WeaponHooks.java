package fi.jakojaannos.roguelite.game.weapons;

import java.util.function.Supplier;

import fi.jakojaannos.roguelite.game.weapons.events.*;

/**
 * Event hooks for weapon modules. Allows weapon modules to hook into different weapon events and execute behavior
 * during them. Allows registering module states.
 */
public interface WeaponHooks {
    void reload(WeaponEventHandler<ReloadEvent> onReload, Phase phase);

    void triggerPull(WeaponEventHandler<TriggerPullEvent> onTriggerPull, Phase phase);

    void triggerRelease(WeaponEventHandler<TriggerReleaseEvent> onTriggerRelease, Phase phase);

    void weaponFire(WeaponEventHandler<WeaponFireEvent> onWeaponFire, Phase phase);

    void weaponEquip(WeaponEventHandler<WeaponEquipEvent> onEquip, Phase phase);

    void weaponUnequip(WeaponEventHandler<WeaponUnequipEvent> onUnequip, Phase phase);

    void weaponStateQuery(WeaponEventHandler<WeaponStateQuery> query, Phase phase);

    <TState> void registerStateFactory(Class<TState> stateClass, Supplier<TState> factory);
}
