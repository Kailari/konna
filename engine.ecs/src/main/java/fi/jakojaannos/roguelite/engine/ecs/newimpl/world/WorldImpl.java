package fi.jakojaannos.roguelite.engine.ecs.newimpl.world;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.World;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.components.ComponentStorage;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.resources.ResourceStorage;

public class WorldImpl implements World {
    @Deprecated
    private final LegacyCompat compat;

    private final ComponentStorage componentStorage;
    private final ResourceStorage resourceStorage;

    private final int capacity;

    private int nEntities;

    @Override
    public LegacyCompat getCompatibilityLayer() {
        return this.compat;
    }

    @Override
    public int getEntityCount() {
        return this.nEntities;
    }

    @Override
    public ComponentStorage getComponents() {
        return this.componentStorage;
    }

    public WorldImpl() {
        this.capacity = 256;
        this.nEntities = 0;

        this.resourceStorage = new ResourceStorage();
        this.componentStorage = new ComponentStorage(this.capacity);

        this.compat = new LegacyCompat(this);
    }

    @Override
    public void registerResource(final Object resource) {
        this.resourceStorage.register(resource);
    }

    @Override
    public <TResource> void registerResource(final Class<TResource> resourceClass, final TResource resource) {
        this.resourceStorage.register(resourceClass, resource);
    }

    @Override
    public EntityHandle createEntity(final Object... components) {
        final var handle = new EntityHandleImpl(this.nEntities, this.componentStorage);
        this.nEntities++;
        return handle;
    }

    @Override
    public <TResource> TResource fetchResource(final Class<?> resourceClass) {
        return this.resourceStorage.fetch(resourceClass);
    }
}
