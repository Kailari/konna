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
        assert !this.resources.containsKey(resourceClass) : "Resource type " + resourceClass.getSimpleName() + "is already registered!";
        this.resources.put(resourceClass, resource);
    }

    public Object[] fetchResources(final Class<?>[] resourceTypes) {
        final var paramResources = new Object[resourceTypes.length];

        for (int paramIndex = 0; paramIndex < paramResources.length; ++paramIndex) {
            final Class<?> resourceType = resourceTypes[paramIndex];
            assert this.resources.containsKey(resourceType) : "Unregistered resource type: " + resourceType.getSimpleName();
            paramResources[paramIndex] = this.resources.get(resourceType);
        }

        return paramResources;
    }
}
