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

    public <TComponent> void register(final Class<TComponent> componentClass) {
        if (this.components.containsKey(componentClass)) {
            throw new IllegalArgumentException(String.format(
                    "Component type %s is already registered!",
                    componentClass.getSimpleName()));
        }
        this.components.put(componentClass,
                            constructComponentArray(componentClass, this.capacity));
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

    @SuppressWarnings("unchecked")
    private <TComponent> TComponent[] getStorage(final Class<TComponent> componentClass) {
        return (TComponent[])
                this.components.computeIfAbsent(componentClass,
                                                key -> constructComponentArray(key, this.capacity));
    }

    public <TComponent> Optional<TComponent> get(final int id, final Class<TComponent> componentClass) {
        return Optional.ofNullable(getStorage(componentClass)[id]);
    }

    @SuppressWarnings("unchecked")
    private static <TComponent> TComponent[] constructComponentArray(
            final Class<TComponent> componentClass,
            final int capacity
    ) {
        return (TComponent[]) Array.newInstance(componentClass, capacity);
    }
}
