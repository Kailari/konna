package fi.jakojaannos.roguelite.game.weapons;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import fi.jakojaannos.roguelite.game.weapons.events.*;

public class ModularWeapon {
    private final List<InternalHandler<ReloadEvent>> reloadListeners;
    private final List<InternalHandler<TriggerPullEvent>> triggerPullListeners;
    private final List<InternalHandler<TriggerReleaseEvent>> triggerReleaseListeners;
    private final List<InternalHandler<WeaponFireEvent>> weaponFireListeners;
    private final List<InternalHandler<WeaponEquipEvent>> equipListeners;
    private final List<InternalHandler<WeaponUnequipEvent>> unequipListeners;
    private final List<InternalHandler<WeaponStateQuery>> queryListeners;

    private final Map<Class<?>, Object> attributes = new HashMap<>();
    private final Map<Class<?>, Supplier<?>> stateFactories = new HashMap<>();

    public Map<Class<?>, Object> getAttributes() {
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
        this.queryListeners = new ArrayList<>();

        final var hooks = new Hooks();
        for (final var entry : modules) {
            entry.module.register(hooks, entry.attributes);
            this.attributes.put(entry.attributes.getClass(), entry.attributes);
        }

        this.reloadListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.triggerPullListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.triggerReleaseListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.weaponFireListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.unequipListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.equipListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        this.queryListeners.sort(Comparator.comparing(InternalHandler::getPhase));
    }

    public void reload(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var reloadEvent = new ReloadEvent();
        for (final var handler : this.reloadListeners) {
            handler.handle(weapon.getInstance(), reloadEvent, info);

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
            handler.handle(weapon.getInstance(), triggerPullEvent, info);

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
            handler.handle(weapon.getInstance(), triggerReleaseEvent, info);

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
            handler.handle(weapon.getInstance(), fireEvent, info);

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
            handler.handle(weapon.getInstance(), equipEvent, info);

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
            handler.handle(weapon.getInstance(), unequipEvent, info);

            if (unequipEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public WeaponStateQuery weaponStateQuery(
            final InventoryWeapon weapon,
            final ActionInfo info
    ) {
        final var query = new WeaponStateQuery();
        for (final var handler : this.queryListeners) {
            handler.handle(weapon.getInstance(), query, info);
        }
        return query;
    }

    public Map<Class<?>, Object> constructStates() {
        return this.stateFactories.entrySet()
                                  .stream()
                                  .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                                                        entry -> entry.getValue().get()));
    }

    public static record Module<TAttributes>(
            WeaponModule<TAttributes>module,
            TAttributes attributes
    ) {}

    private final class Hooks implements WeaponHooks {
        @Override
        public void registerReload(
                final WeaponEventHandler<ReloadEvent> onReload,
                final Phase phase
        ) {
            ModularWeapon.this.reloadListeners.add(new InternalHandler<>(phase, onReload));
        }

        @Override
        public void registerTriggerPull(
                final WeaponEventHandler<TriggerPullEvent> onTriggerPull,
                final Phase phase
        ) {
            ModularWeapon.this.triggerPullListeners.add(new InternalHandler<>(phase, onTriggerPull));
        }

        @Override
        public void registerTriggerRelease(
                final WeaponEventHandler<TriggerReleaseEvent> onTriggerRelease,
                final Phase phase
        ) {
            ModularWeapon.this.triggerReleaseListeners.add(new InternalHandler<>(phase, onTriggerRelease));
        }

        @Override
        public void registerWeaponFire(
                final WeaponEventHandler<WeaponFireEvent> onWeaponFire,
                final Phase phase
        ) {
            ModularWeapon.this.weaponFireListeners.add(new InternalHandler<>(phase, onWeaponFire));
        }

        @Override
        public void registerWeaponEquip(
                final WeaponEventHandler<WeaponEquipEvent> onEquip,
                final Phase phase
        ) {
            ModularWeapon.this.equipListeners.add(new InternalHandler<>(phase, onEquip));
        }

        @Override
        public void registerWeaponUnequip(
                final WeaponEventHandler<WeaponUnequipEvent> onUnequip,
                final Phase phase
        ) {
            ModularWeapon.this.unequipListeners.add(new InternalHandler<>(phase, onUnequip));
        }

        @Override
        public void registerWeaponStateQuery(
                final WeaponEventHandler<WeaponStateQuery> query,
                final Phase phase
        ) {
            ModularWeapon.this.queryListeners.add(new InternalHandler<>(phase, query));
        }

        @Override
        public <TState> void registerStateFactory(final Class<TState> stateClass, final Supplier<TState> factory) {
            // TODO: Throw with descriptive error message if factory already exists
            //       (multiple modules registered the same state)
            ModularWeapon.this.stateFactories.put(stateClass, factory);
        }
    }
}
