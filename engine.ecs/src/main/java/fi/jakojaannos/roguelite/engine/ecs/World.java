package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.legacy.LegacyWorld;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ProvidedResource;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Resource;
import fi.jakojaannos.roguelite.engine.ecs.world.LegacyCompat;
import fi.jakojaannos.roguelite.engine.ecs.world.WorldImpl;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ComponentStorage;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ResourceStorage;

public interface World extends LegacyWorld {
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

    <TResource> void registerResource(Class<? super TResource> resourceClass, TResource resource);

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
    default <TResource extends ProvidedResource> void provideResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        getCompatibilityLayer().provideResource(resourceClass, resource);
    }
}
