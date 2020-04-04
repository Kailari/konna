package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.world.WorldImpl;

/**
 * @deprecated This interface is deprecated. The new implementation should be used instead.
 */
@Deprecated
public interface World {
    /**
     * Gets the entity/component manager for this world.
     *
     * @return entity/component manager instance for this world
     */
    @Deprecated
    EntityManager getEntityManager();

    @Deprecated
    static World createNew(EntityManager entityManager) {
        return new WorldImpl(entityManager);
    }

    /**
     * Creates or gets the resource of given type.
     *
     * @param resourceType class of the resource to get
     * @param <TResource>  type of the resource to get
     *
     * @return the resource of given type
     */
    @Deprecated
    <TResource extends Resource> TResource getOrCreateResource(Class<TResource> resourceType);

    /**
     * Manually assigns a resource to a specific value.
     *
     * @param resourceClass type of the resource to create
     * @param resource      resource to create
     */
    @Deprecated
    <TResource extends Resource> void createOrReplaceResource(
            Class<TResource> resourceClass,
            TResource resource
    );

    /**
     * Manually assigns a provided resource to a specific value.
     *
     * @param resourceClass type of the resource to create
     * @param resource      resource to create
     */
    @Deprecated
    <TResource extends ProvidedResource> void provideResource(
            Class<TResource> resourceClass,
            TResource resource
    );

    @Deprecated
    <TResource extends ProvidedResource> TResource getResource(Class<TResource> resourceType);
}
