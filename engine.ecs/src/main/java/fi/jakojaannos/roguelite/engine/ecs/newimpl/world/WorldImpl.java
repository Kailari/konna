package fi.jakojaannos.roguelite.engine.ecs.newimpl.world;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.World;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.components.ComponentStorage;

public class WorldImpl implements World {
    private final ComponentStorage componentStorage;

    @Override
    public EntityManager getEntityManager() {
        return null;
    }

    public WorldImpl() {
        this.componentStorage = new ComponentStorage();
    }

    @Override
    public <TResource extends Resource> TResource getOrCreateResource(final Class<TResource> resourceType) {
        return null;
    }

    @Override
    public <TResource extends Resource> void createOrReplaceResource(
            final Class<TResource> tResourceClass,
            final TResource resource
    ) {

    }

    @Override
    public <TResource extends ProvidedResource> void provideResource(
            final Class<TResource> tResourceClass, final TResource resource
    ) {

    }

    @Override
    public <TResource extends ProvidedResource> TResource getResource(final Class<TResource> resourceType) {
        return null;
    }
}
