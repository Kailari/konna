package fi.jakojaannos.roguelite.engine.ecs.newecs;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.newecs.components.ComponentStorage;
import fi.jakojaannos.roguelite.engine.ecs.newecs.resources.ResourceStorage;
import fi.jakojaannos.roguelite.engine.ecs.newecs.world.LegacyCompat;
import fi.jakojaannos.roguelite.engine.ecs.newecs.world.WorldImpl;

public interface World extends fi.jakojaannos.roguelite.engine.ecs.World {
    LegacyCompat getCompatibilityLayer();

    ComponentStorage getComponents();

    ResourceStorage getResources();

    @Override
    @Deprecated
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

    <TResource> TResource fetchResource(Class<TResource> resourceClass);

    void destroyEntity(EntityHandle handle);

    void commitEntityModifications();

    EntityHandle getEntity(int entityId);

    @Override
    @Deprecated
    default <TResource extends Resource> TResource getOrCreateResource(
            final Class<TResource> resourceType
    ) {
        return getCompatibilityLayer().getOrCreateResource(resourceType);
    }

    @Override
    @Deprecated
    default <TResource extends Resource> void createOrReplaceResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        getCompatibilityLayer().createOrReplaceResource(resourceClass, resource);
    }

    @Override
    @Deprecated
    default <TResource extends ProvidedResource> void provideResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        getCompatibilityLayer().provideResource(resourceClass, resource);
    }

    @Override
    @Deprecated
    default <TResource extends ProvidedResource> TResource getResource(
            final Class<TResource> resourceType
    ) {
        return getCompatibilityLayer().getResource(resourceType);
    }
}
