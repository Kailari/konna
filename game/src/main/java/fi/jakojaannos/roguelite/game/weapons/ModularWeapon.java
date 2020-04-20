package fi.jakojaannos.roguelite.game.weapons;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import fi.jakojaannos.roguelite.game.weapons.events.*;

/**
 * Internal representation of a weapon. Collection of modules attached to an assortment of hooks. Managed through {@link
 * fi.jakojaannos.roguelite.game.data.resources.Weapons Weapons} resource.
 */
public class ModularWeapon {
    private final List<InternalHandler<ReloadEvent>> reloadListeners;
    private final List<InternalHandler<TriggerPullEvent>> triggerPullListeners;
    private final List<InternalHandler<TriggerReleaseEvent>> triggerReleaseListeners;
    private final List<InternalHandler<WeaponFireEvent>> weaponFireListeners;
    private final List<InternalHandler<WeaponEquipEvent>> equipListeners;
    private final List<InternalHandler<WeaponUnequipEvent>> unequipListeners;
    private final List<InternalHandler<WeaponStateQuery>> queryListeners;

    private final Map<Class<?>, Object> attributes;
    private final Map<Class<?>, Supplier<?>> stateFactories;

    public Map<Class<?>, Object> getAttributes() {
        return this.attributes;
    }

    @SuppressWarnings("rawtypes")
    public ModularWeapon(final Module... modules) {
        final var hooks = registerHooks(modules);

        this.attributes = Collections.unmodifiableMap(hooks.attributes);
        this.stateFactories = Collections.unmodifiableMap(hooks.stateFactories);
        this.reloadListeners = List.copyOf(hooks.reloadListeners);
        this.triggerPullListeners = List.copyOf(hooks.triggerPullListeners);
        this.triggerReleaseListeners = List.copyOf(hooks.triggerReleaseListeners);
        this.weaponFireListeners = List.copyOf(hooks.weaponFireListeners);
        this.unequipListeners = List.copyOf(hooks.unequipListeners);
        this.equipListeners = List.copyOf(hooks.equipListeners);
        this.queryListeners = List.copyOf(hooks.queryListeners);
    }

    public void reload(final Weapon instance, final ActionInfo info) {
        final var reloadEvent = new ReloadEvent();
        for (final var handler : this.reloadListeners) {
            handler.handle(instance, reloadEvent, info);

            if (reloadEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void pullTrigger(final Weapon instance, final ActionInfo info) {
        final var triggerPullEvent = new TriggerPullEvent();
        for (final var handler : this.triggerPullListeners) {
            handler.handle(instance, triggerPullEvent, info);

            if (triggerPullEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void releaseTrigger(final Weapon instance, final ActionInfo info) {
        final var triggerReleaseEvent = new TriggerReleaseEvent();
        for (final var handler : this.triggerReleaseListeners) {
            handler.handle(instance, triggerReleaseEvent, info);

            if (triggerReleaseEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void fire(final Weapon instance, final ActionInfo info) {
        final var fireEvent = new WeaponFireEvent();
        for (final var handler : this.weaponFireListeners) {
            handler.handle(instance, fireEvent, info);

            if (fireEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void equip(final Weapon instance, final ActionInfo info) {
        final var equipEvent = new WeaponEquipEvent();
        for (final var handler : this.equipListeners) {
            handler.handle(instance, equipEvent, info);

            if (equipEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public void unequip(final Weapon instance, final ActionInfo info) {
        final var unequipEvent = new WeaponUnequipEvent();
        for (final var handler : this.unequipListeners) {
            handler.handle(instance, unequipEvent, info);

            if (unequipEvent.isCancelled() && handler.getPhase() == Phase.CHECK) {
                break;
            }
        }
    }

    public WeaponStateQuery weaponStateQuery(final Weapon instance, final ActionInfo info) {
        final var query = new WeaponStateQuery();
        for (final var handler : this.queryListeners) {
            handler.handle(instance, query, info);
        }
        return query;
    }

    public Map<Class<?>, Object> constructStates() {
        return this.stateFactories.entrySet()
                                  .stream()
                                  .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey,
                                                                        entry -> entry.getValue().get()));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Hooks registerHooks(final Module[] modules) {
        final var hooks = new Hooks();
        for (final var entry : modules) {
            entry.module.register(hooks, entry.attributes);
            hooks.attributes.put(entry.attributes.getClass(), entry.attributes);
        }
        hooks.sort();
        return hooks;
    }

    public static record Module<TAttributes>(
            WeaponModule<TAttributes>module,
            TAttributes attributes
    ) {}

    private final record Hooks(
            List<InternalHandler<ReloadEvent>>reloadListeners,
            List<InternalHandler<TriggerPullEvent>>triggerPullListeners,
            List<InternalHandler<TriggerReleaseEvent>>triggerReleaseListeners,
            List<InternalHandler<WeaponFireEvent>>weaponFireListeners,
            List<InternalHandler<WeaponEquipEvent>>equipListeners,
            List<InternalHandler<WeaponUnequipEvent>>unequipListeners,
            List<InternalHandler<WeaponStateQuery>>queryListeners,
            Map<Class<?>, Supplier<?>>stateFactories,
            Map<Class<?>, Object>attributes
    ) implements WeaponHooks {
        Hooks() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new HashMap<>(), new HashMap<>());
        }

        @Override
        public void reload(final WeaponEventHandler<ReloadEvent> onReload, final Phase phase) {
            this.reloadListeners.add(new InternalHandler<>(phase, onReload));
        }

        @Override
        public void triggerPull(final WeaponEventHandler<TriggerPullEvent> onTriggerPull, final Phase phase) {
            this.triggerPullListeners.add(new InternalHandler<>(phase, onTriggerPull));
        }

        @Override
        public void triggerRelease(
                final WeaponEventHandler<TriggerReleaseEvent> onTriggerRelease,
                final Phase phase
        ) {
            this.triggerReleaseListeners.add(new InternalHandler<>(phase, onTriggerRelease));
        }

        @Override
        public void weaponFire(final WeaponEventHandler<WeaponFireEvent> onWeaponFire, final Phase phase) {
            this.weaponFireListeners.add(new InternalHandler<>(phase, onWeaponFire));
        }

        @Override
        public void weaponEquip(final WeaponEventHandler<WeaponEquipEvent> onEquip, final Phase phase) {
            this.equipListeners.add(new InternalHandler<>(phase, onEquip));
        }

        @Override
        public void weaponUnequip(final WeaponEventHandler<WeaponUnequipEvent> onUnequip, final Phase phase) {
            this.unequipListeners.add(new InternalHandler<>(phase, onUnequip));
        }

        @Override
        public void weaponStateQuery(final WeaponEventHandler<WeaponStateQuery> query, final Phase phase) {
            this.queryListeners.add(new InternalHandler<>(phase, query));
        }

        @Override
        public <TState> void registerStateFactory(final Class<TState> stateClass, final Supplier<TState> factory) {
            if (this.stateFactories.containsKey(stateClass)) {
                throw new IllegalStateException("State already registered (" + stateClass.getSimpleName()
                                                + ")! Multiple modules registered factories for the same state!");
            }
            this.stateFactories.put(stateClass, factory);
        }

        public void sort() {
            this.reloadListeners.sort(Comparator.comparing(InternalHandler::getPhase));
            this.triggerPullListeners.sort(Comparator.comparing(InternalHandler::getPhase));
            this.triggerReleaseListeners.sort(Comparator.comparing(InternalHandler::getPhase));
            this.weaponFireListeners.sort(Comparator.comparing(InternalHandler::getPhase));
            this.unequipListeners.sort(Comparator.comparing(InternalHandler::getPhase));
            this.equipListeners.sort(Comparator.comparing(InternalHandler::getPhase));
            this.queryListeners.sort(Comparator.comparing(InternalHandler::getPhase));
        }
    }
}
