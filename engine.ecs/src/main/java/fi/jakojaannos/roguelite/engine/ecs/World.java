package fi.jakojaannos.roguelite.engine.ecs;

import java.util.function.Function;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.world.WorldImpl;

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
public interface World {
    /**
     * @deprecated Getter for legacy-compatible entity manager for this world. Due for removal once legacy systems are
     *         refactored to the new ECS.
     */
    @Deprecated
    EntityManager getEntityManager();

    /**
     * Gets the number of entities currently in the world.
     *
     * @return the number of entities
     */
    int getEntityCount();

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
     * Creates a spliterator for iterating over the entities with specified components pre-fetched.
     *
     * @param componentClasses required component classes
     * @param excluded         inversion table for component classes. Must have exact same length as
     *                         <code>componentClasses</code>
     * @param optional         lookup table for checking if components are optional
     * @param dataFactory      factory for producing entity data instances
     * @param <TEntityData>    entity data container type. Structure for containing the pre-fetched components
     *
     * @return spliterator for iterating entities matching the specified requirements
     */
    <TEntityData> Stream<EntityDataHandle<TEntityData>> iterateEntities(
            Class<?>[] componentClasses,
            boolean[] excluded,
            boolean[] optional,
            Function<Object[], TEntityData> dataFactory
    );

    /**
     * Marks and queues the given entity for destruction. The same as calling {@link EntityHandle#destroy()}. After this
     * call, {@link EntityHandle#isPendingRemoval()} is guaranteed to immediately start returning <code>true</code>.
     * This should not generally be relied upon, though, to avoid race-conditions.
     * <p>
     * The entity will not be destroyed until the {@link #commitEntityModifications()} is called the next time.
     *
     * @param handle the entity to destroy.
     */
    void destroyEntity(EntityHandle handle);

    /**
     * Flushes all pending add/destroy entity operations.
     */
    void commitEntityModifications();

    /**
     * Gets the entity with the given entity ID. Not very useful for general use as entity IDs are unstable and may
     * frequently change as entities are created/destroyed.
     *
     * @param entityId id of the entity to get
     *
     * @return handle to the entity
     */
    EntityHandle getEntity(int entityId);

    /**
     * Clears all entities from this world.
     */
    void clearAllEntities();
}
