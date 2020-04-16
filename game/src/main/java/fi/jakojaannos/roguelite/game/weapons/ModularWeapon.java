package fi.jakojaannos.roguelite.game.weapons;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ModularWeapon {
    private final List<InternalHandler<?, ?, ReloadEvent>> reloadListeners;
    private final List<InternalHandler<?, ?, TriggerPullEvent>> triggerPullListeners;
    private final List<InternalHandler<?, ?, TriggerReleaseEvent>> triggerReleaseListeners;
    private final List<InternalHandler<?, ?, WeaponFireEvent>> weaponFireListeners;
    private final List<InternalHandler<?, ?, WeaponEquipEvent>> equipListeners;
    private final List<InternalHandler<?, ?, WeaponUnequipEvent>> unequipListeners;

    private final WeaponAttributes attributes;

    public WeaponAttributes getAttributes() {
        return this.attributes;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public ModularWeapon(final Module... modules) {
        this.reloadListeners = new ArrayList<>();
        this.triggerPullListeners = new ArrayList<>();
        this.triggerReleaseListeners = new ArrayList<>();
        this.weaponFireListeners = new ArrayList<>();
        this.equipListeners = new ArrayList<>();
        this.unequipListeners = new ArrayList<>();

        this.attributes = new WeaponAttributes();

        final var hooks = new Hooks();
        for (final var entry : modules) {
            entry.module.register(hooks);
            // XXX: We *could* further pass this thing down to get rid of uncheckedness, but entry
            //      signature ensures that the types must pass, so rawtype is OK workaround here.
            this.attributes.put((Class) entry.module.getClass(), entry.attributes);
        }

        this.reloadListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.triggerPullListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.triggerReleaseListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.weaponFireListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.unequipListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.equipListeners.sort(Comparator.comparing(InternalHandler::getPhase));
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

    public void equip(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var equipEvent = new WeaponEquipEvent();
        for (final var handler : this.equipListeners) {
            handler.handle(weapon, equipEvent, info);

            if (equipEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void unequip(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var unequipEvent = new WeaponUnequipEvent();
        for (final var handler : this.unequipListeners) {
            handler.handle(weapon, unequipEvent, info);

            if (unequipEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public static record Module<TState, TAttributes>(
            WeaponModule<TState, TAttributes>module,
            TAttributes attributes
    ) {}

    private final class Hooks implements WeaponHooks {
        @Override
        public <TState, TAttributes> void registerReload(
                final WeaponModule<TState, TAttributes> module,
                final WeaponEventHandler<TState, TAttributes, ReloadEvent> onReload,
                final Phase phase
        ) {
            ModularWeapon.this.reloadListeners.add(new InternalHandler<>(module, phase, onReload));
        }

        @Override
        public <TState, TAttributes> void registerTriggerPull(
                final WeaponModule<TState, TAttributes> module,
                final WeaponEventHandler<TState, TAttributes, TriggerPullEvent> onTriggerPull,
                final Phase phase
        ) {
            ModularWeapon.this.triggerPullListeners.add(new InternalHandler<>(module, phase, onTriggerPull));
        }

        @Override
        public <TState, TAttributes> void registerTriggerRelease(
                final WeaponModule<TState, TAttributes> module,
                final WeaponEventHandler<TState, TAttributes, TriggerReleaseEvent> onTriggerRelease,
                final Phase phase
        ) {
            ModularWeapon.this.triggerReleaseListeners.add(new InternalHandler<>(module, phase, onTriggerRelease));
        }

        @Override
        public <TState, TAttributes> void registerWeaponFire(
                final WeaponModule<TState, TAttributes> module,
                final WeaponEventHandler<TState, TAttributes, WeaponFireEvent> onWeaponFire,
                final Phase phase
        ) {
            ModularWeapon.this.weaponFireListeners.add(new InternalHandler<>(module, phase, onWeaponFire));
        }

        @Override
        public <TState, TAttributes> void registerWeaponEquip(
                final WeaponModule<TState, TAttributes> module,
                final WeaponEventHandler<TState, TAttributes, WeaponEquipEvent> onEquip,
                final Phase phase
        ) {
            ModularWeapon.this.equipListeners.add(new InternalHandler<>(module, phase, onEquip));
        }

        @Override
        public <TState, TAttributes> void registerWeaponUnequip(
                final WeaponModule<TState, TAttributes> module,
                final WeaponEventHandler<TState, TAttributes, WeaponUnequipEvent> onUnequip,
                final Phase phase
        ) {
            ModularWeapon.this.unequipListeners.add(new InternalHandler<>(module, phase, onUnequip));
        }
    }
}
