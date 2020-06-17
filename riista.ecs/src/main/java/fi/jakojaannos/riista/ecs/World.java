package fi.jakojaannos.riista.ecs;

import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.riista.ecs.world.WorldImpl;

/**
 * A collection of resources and entities with their components. Basically a convenient wrapper for a huge pile of data.
 * Allows creation and destruction of entities and registering and fetching resources.
 * <p>
 * A {@link SystemDispatcher dispatcher} with collection of {@link EcsSystem systems} are generally used for
 * manipulating a world.
 *
 * @see EntityHandle
 * @see SystemDispatcher
 * @see EcsSystem
 */
public interface World extends Entities {
    /**
     * Creates a new world instance.
     *
     * @return a new world
     */
    static World createNew() {
        return new WorldImpl();
    }

    /**
     * Registers a resource to the world. Re-registering already registered resource is considered an error.
     * <p>
     * This overload registers the resource assuming it has the exact class it will be used as. That is, this is
     * equivalent to registering with <code>registerResource(resource.getClass(), resource)</code>.
     *
     * @param resource resource to register.
     */
    void registerResource(Object resource);

    /**
     * Registers a resource to the world. Re-registering already registered resource is considered an error.
     * <p>
     * This overload registers the resource, but allows using an abstract base class for its "key". This then allows
     * hiding the actual implementation from the systems. E.g. use this to hide non-public API of a class from systems
     * when defining resources. Common pattern is to define an interface with the public methods and then using that
     * interface as the key resource class.
     *
     * @param resourceClass the key-resource class the systems will use for fetching this resource
     * @param resource      resource to register.
     * @param <TResource>   type of the resource
     */
    <TResource> void registerResource(Class<? super TResource> resourceClass, TResource resource);

    /**
     * Creates a new entity with given components. Providing components through the constructor should be preferred over
     * calling {@link EntityHandle#addComponent(Object)}.
     * <p>
     * Note that the entity is not spawned to the world until {@link #commitEntityModifications()} is called the next
     * time. For systems ticked using dispatcher, this happens automatically after the system tick is finished and
     * generally should not be called manually.
     *
     * @param components components to add to the entity.
     *
     * @return handle to the newly created entity
     */
    EntityHandle createEntity(Object... components);

    /**
     * Fetches a single resource from the world. Fetching a non-registered resource is considered an error, so make sure
     * to register your resources first during game state initialization.
     *
     * @param resourceClass resource to fetch
     * @param <TResource>   type of the resource
     *
     * @return the resource
     */
    <TResource> TResource fetchResource(Class<TResource> resourceClass);

    /**
     * Fetches multiple resources from this world. Resources are guaranteed to have the same order and classes as
     * specified in the the parameter array.
     *
     * @param resourceClasses resources to fetch
     *
     * @return an array containing the requested resources
     */
    Object[] fetchResources(Class<?>[] resourceClasses);

    /**
     * Flushes all pending add/destroy entity operations.
     */
    void commitEntityModifications();

    /**
     * Clears all entities from this world.
     */
    void clearAllEntities();

    <TResource> void replaceResource(Class<? super TResource> resourceClass, TResource resource);
}
