package fi.jakojaannos.riista.ecs.world.storage;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Supplier;

public final class ComponentStorage {
    private final Map<Class<?>, Object[]> components = new HashMap<>();
    private int capacity;

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

    public <TComponent> TComponent addOrGet(
            final int id,
            final Class<TComponent> componentClass,
            final Supplier<TComponent> supplier
    ) {
        final var storage = getStorage(componentClass);
        if (storage[id] == null) {
            storage[id] = supplier.get();
        }

        return storage[id];
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
        return getStorage(componentClass)[id] != null;
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

    public void resize(final int newCapacity) {
        this.capacity = newCapacity;
        this.components.replaceAll((componentClass, storage) -> Arrays.copyOf(storage, newCapacity));
    }

    public void clear() {
        // HACK: getStorage re-constructs the component storages after clearing. Clear should be so
        //       rare op that the implied performance cost should not matter. In case this becomes
        //       an issue, do something smarter here.
        this.components.clear();
    }

    @SuppressWarnings("unchecked")
    private static <TComponent> TComponent[] constructComponentArray(
            final Class<TComponent> componentClass,
            final int capacity
    ) {
        return (TComponent[]) Array.newInstance(componentClass, capacity);
    }
}
