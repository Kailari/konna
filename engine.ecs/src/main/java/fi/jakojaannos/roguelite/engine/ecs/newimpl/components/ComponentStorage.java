package fi.jakojaannos.roguelite.engine.ecs.newimpl.components;

// TODO: add/remove must happen *after* iteration (of a single system) has finished. Otherwise
//       execution order might affect which entities are iterated

import java.util.HashMap;
import java.util.Map;

public final class ComponentStorage {
    private final Map<Class<?>, Object[]> components = new HashMap<>();

    public <TComponent> void register(
            final Class<TComponent> componentClass,
            final TComponent[] components
    ) {
        assert !this.components.containsKey(componentClass) : "Component type " + componentClass.getSimpleName() + "is already registered!";
        this.components.put(componentClass, components);
    }

    public Object[][] fetchStorages(
            final Class<?>[] componentTypes
    ) {
        final var paramStorages = new Object[componentTypes.length][];

        for (int paramIndex = 0; paramIndex < paramStorages.length; ++paramIndex) {
            final Class<?> paramType = componentTypes[paramIndex];
            assert this.components.containsKey(paramType) : "Unregistered component type: " + paramType.getSimpleName();
            paramStorages[paramIndex] = this.components.get(paramType);
        }
        return paramStorages;
    }
}
