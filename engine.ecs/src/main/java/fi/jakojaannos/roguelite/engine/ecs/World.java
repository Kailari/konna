package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.world.LegacyCompat;
import fi.jakojaannos.roguelite.engine.ecs.world.WorldImpl;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ComponentStorage;
import fi.jakojaannos.roguelite.engine.ecs.world.storage.ResourceStorage;

public interface World {
    LegacyCompat getCompatibilityLayer();

    ComponentStorage getComponents();

    ResourceStorage getResources();

    @Deprecated
    default EntityManager getEntityManager() {
        return getCompatibilityLayer();
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
}
