package fi.jakojaannos.roguelite.engine.ecs.legacy;

/**
 * @deprecated This interface is deprecated. The new implementation should be used instead.
 */
@Deprecated
public interface LegacyWorld {
    /**
     * Gets the entity/component manager for this world.
     *
     * @return entity/component manager instance for this world
     */
    @Deprecated
    EntityManager getEntityManager();

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
}
