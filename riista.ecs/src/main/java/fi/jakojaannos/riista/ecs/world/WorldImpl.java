package fi.jakojaannos.riista.ecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.LogCategories;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.riista.ecs.world.storage.EntityStorage;
import fi.jakojaannos.riista.ecs.world.storage.ResourceStorage;

public class WorldImpl implements World {
    private static final Logger LOG = LoggerFactory.getLogger(World.class);

    private final ResourceStorage resourceStorage;
    private final EntityStorage entityStorage;

    public WorldImpl() {
        this.resourceStorage = new ResourceStorage();
        this.entityStorage = new EntityStorage();

        this.registerResource(Entities.class, this);
    }

    @Override
    public void registerResource(final Object resource) {
        this.resourceStorage.register(resource);
    }

    @Override
    public <TResource> void replaceResource(final Class<? super TResource> resourceClass, final TResource resource) {
        this.resourceStorage.registerOrReplace(resourceClass, resource);
    }

    @Override
    public <TResource> void registerResource(final Class<? super TResource> resourceClass, final TResource resource) {
        this.resourceStorage.register(resourceClass, resource);
    }

    @Override
    public synchronized EntityHandle createEntity(final Object... components) {
        return this.entityStorage.createEntity(components);
    }

    @Override
    public synchronized void clearAllEntities() {
        LOG.warn(LogCategories.ENTITY_LIFECYCLE, "Clearing all entities!");
        this.entityStorage.clear();
    }

    @Override
    public synchronized void commitEntityModifications() {
        this.entityStorage.commitModifications();
    }

    @Override
    public <TResource> TResource fetchResource(final Class<TResource> resourceClass) {
        return this.resourceStorage.fetch(resourceClass);
    }

    @Override
    public Object[] fetchResources(final Class<?>[] resourceClasses) {
        return this.resourceStorage.fetch(resourceClasses);
    }

    @Override
    public <TEntityData> Stream<EntityDataHandle<TEntityData>> iterateEntities(
            final Class<?>[] componentClasses,
            final boolean[] excluded,
            final boolean[] optional,
            final Function<Object[], TEntityData> dataFactory,
            final boolean parallel
    ) {
        return this.entityStorage.stream(componentClasses, excluded, optional, dataFactory, parallel);
    }
}
