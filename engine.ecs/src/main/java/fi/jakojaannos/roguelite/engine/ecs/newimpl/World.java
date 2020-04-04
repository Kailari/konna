package fi.jakojaannos.roguelite.engine.ecs.newimpl;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.components.ComponentStorage;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.world.LegacyCompat;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.world.WorldImpl;

public interface World extends fi.jakojaannos.roguelite.engine.ecs.World {
    LegacyCompat getCompatibilityLayer();

    ComponentStorage getComponents();

    @Override
    default EntityManager getEntityManager() {
        return getCompatibilityLayer().getEntityManager();
    }

    int getEntityCount();

    static World createNew() {
        return new WorldImpl();
    }

    void registerResource(Object resource);

    <TResource> void registerResource(Class<TResource> resourceClass, TResource resource);

    EntityHandle createEntity(Object... components);

    <TResource> TResource fetchResource(Class<?> resourceClass);

    @Override
    default <TResource extends Resource> TResource getOrCreateResource(
            final Class<TResource> resourceType
    ) {
        return getCompatibilityLayer().getOrCreateResource(resourceType);
    }

    @Override
    default <TResource extends Resource> void createOrReplaceResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        getCompatibilityLayer().createOrReplaceResource(resourceClass, resource);
    }

    @Override
    default <TResource extends ProvidedResource> void provideResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        getCompatibilityLayer().provideResource(resourceClass, resource);
    }

    @Override
    default <TResource extends ProvidedResource> TResource getResource(
            final Class<TResource> resourceType
    ) {
        return getCompatibilityLayer().getResource(resourceType);
    }
}
