package fi.jakojaannos.roguelite.game.weapons;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModularWeapon implements WeaponHooks {
    private final List<InternalHandler<?, ?, ReloadEvent>> reloadListeners;
    private final List<InternalHandler<?, ?, TriggerPullEvent>> triggerPullListeners;
    private final List<InternalHandler<?, ?, TriggerReleaseEvent>> triggerReleaseListeners;
    private final List<InternalHandler<?, ?, WeaponFireEvent>> weaponFireListeners;

    public ModularWeapon(final WeaponModule... modules) {
        this.reloadListeners = new ArrayList<>();
        this.triggerPullListeners = new ArrayList<>();
        this.triggerReleaseListeners = new ArrayList<>();
        this.weaponFireListeners = new ArrayList<>();

        for (final var module : modules) {
            module.register(this);
        }

        this.reloadListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.triggerPullListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.triggerReleaseListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.weaponFireListeners.sort(Comparator.comparing(InternalHandler::getPhase));
    }

    public void reload(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var reloadEvent = new ReloadEvent();
        for (final var handler : this.reloadListeners) {
            handler.handle(weapon, reloadEvent, info);

            if (reloadEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void pullTrigger(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var triggerPullEvent = new TriggerPullEvent();
        for (final var handler : this.triggerPullListeners) {
            handler.handle(weapon, triggerPullEvent, info);

            if (triggerPullEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void releaseTrigger(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var triggerReleaseEvent = new TriggerReleaseEvent();
        for (final var handler : this.triggerReleaseListeners) {
            handler.handle(weapon, triggerReleaseEvent, info);

            if (triggerReleaseEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void fire(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var fireEvent = new WeaponFireEvent();
        for (final var handler : this.weaponFireListeners) {
            handler.handle(weapon, fireEvent, info);

            if (fireEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    @Override
    public <TState, TAttributes> void onReload(
            final WeaponModule<TState, TAttributes> module,
            final WeaponEventHandler<TState, TAttributes, ReloadEvent> onReload,
            final Phase phase
    ) {
        this.reloadListeners.add(new InternalHandler<>(module, phase, onReload));
    }

    @Override
    public <TState, TAttributes> void onTriggerPull(
            final WeaponModule<TState, TAttributes> module,
            final WeaponEventHandler<TState, TAttributes, TriggerPullEvent> onTriggerPull,
            final Phase phase
    ) {
        this.triggerPullListeners.add(new InternalHandler<>(module, phase, onTriggerPull));
    }

    @Override
    public <TState, TAttributes> void onTriggerRelease(
            final WeaponModule<TState, TAttributes> module,
            final WeaponEventHandler<TState, TAttributes, TriggerReleaseEvent> onTriggerRelease,
            final Phase phase
    ) {
        this.triggerReleaseListeners.add(new InternalHandler<>(module, phase, onTriggerRelease));
    }

    @Override
    public <TState, TAttributes> void onWeaponFire(
            final WeaponModule<TState, TAttributes> module,
            final WeaponEventHandler<TState, TAttributes, WeaponFireEvent> onWeaponFire,
            final Phase phase
    ) {
        this.weaponFireListeners.add(new InternalHandler<>(module, phase, onWeaponFire));
    }
}
