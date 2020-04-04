package fi.jakojaannos.roguelite.engine.ecs.newimpl.resources;

import java.util.HashMap;
import java.util.Map;

public class ResourceStorage {
    private final Map<Class<?>, Object> resources = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void register(final Object resource) {
        register((Class) resource.getClass(), resource);
    }

    public <TResource> void register(final Class<TResource> resourceClass, final TResource resource) {
        if (this.resources.containsKey(resourceClass)) {
            throw new IllegalArgumentException(String.format(
                    "Resource type %s is already registered!",
                    resourceClass.getSimpleName()));
        }
        this.resources.put(resourceClass, resource);
    }

    @SuppressWarnings("unchecked")
    public <TResource> TResource fetch(final Class<?> resourceClass) {
        if (!this.resources.containsKey(resourceClass)) {
            throw new IllegalArgumentException(String.format(
                    "Unregistered resource type %s!",
                    resourceClass.getSimpleName()));
        }

        return (TResource) this.resources.get(resourceClass);
    }

    public Object[] fetch(final Class<?>[] resourceClasses) {
        final var paramResources = new Object[resourceClasses.length];

        for (int paramIndex = 0; paramIndex < paramResources.length; ++paramIndex) {
            final Class<?> resourceClass = resourceClasses[paramIndex];
            paramResources[paramIndex] = fetch(resourceClass);
        }

        return paramResources;
    }
}
