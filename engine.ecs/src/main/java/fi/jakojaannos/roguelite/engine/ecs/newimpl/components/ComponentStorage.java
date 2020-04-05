package fi.jakojaannos.roguelite.engine.ecs.newimpl.components;

// TODO: add/remove must happen *after* iteration (of a single system) has finished. Otherwise
//       execution order might affect which entities are iterated

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ComponentStorage {
    private final Map<Class<?>, Object[]> components = new HashMap<>();
    private final int capacity;

    public ComponentStorage(final int capacity) {
        this.capacity = capacity;
    }

    public Object[][] fetchStorages(
            final Class<?>[] componentClasses
    ) {
        final var paramStorages = new Object[componentClasses.length][];

        for (int paramIndex = 0; paramIndex < paramStorages.length; ++paramIndex) {
            paramStorages[paramIndex] = getStorage(componentClasses[paramIndex]);
        }
        return paramStorages;
    }

    public <TComponent> boolean add(final int id, final TComponent component) {
        final var componentClass = Objects.requireNonNull(component).getClass();

        final var storage = getStorage(componentClass);
        if (storage[id] != null) {
            return false;
        }

        storage[id] = component;
        return true;
    }

    public <TComponent> boolean remove(final int id, final Class<TComponent> componentClass) {
        final var storage = getStorage(componentClass);
        if (storage[id] == null) {
            return false;
        }

        storage[id] = null;
        return true;
    }

    public <TComponent> Optional<TComponent> get(final int id, final Class<TComponent> componentClass) {
        return Optional.ofNullable(getStorage(componentClass)[id]);
    }

    public boolean has(final int id, final Class<?> componentClass) {
        return get(id, componentClass).isPresent();
    }

    @SuppressWarnings("unchecked")
    private <TComponent> TComponent[] getStorage(final Class<TComponent> componentClass) {
        return (TComponent[])
                this.components.computeIfAbsent(componentClass,
                                                key -> constructComponentArray(key, this.capacity));
    }

    public void moveAndClear(final int from, final int to) {
        if (from == to) {
            for (final var storage : this.components.values()) {
                storage[from] = null;
            }
        } else {
            for (final var storage : this.components.values()) {
                storage[to] = storage[from];
                storage[from] = null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <TComponent> TComponent[] constructComponentArray(
            final Class<TComponent> componentClass,
            final int capacity
    ) {
        return (TComponent[]) Array.newInstance(componentClass, capacity);
    }
}
