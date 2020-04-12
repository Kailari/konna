package fi.jakojaannos.roguelite.engine.ecs;

/**
 * Builder for constructing system requirements. Allows specifying which concrete classes are to be used for system
 * resource, entity data and event requirements passing.
 *
 * @param <TResources>  type to hold resources
 * @param <TEntityData> type to hold entity data
 * @param <TEvents>     type to hold events
 */
public interface Requirements<TResources, TEntityData, TEvents> {
    /**
     * Registers the used concrete entity data class for the system. Class must be a record and its record-components
     * must be valid ECS components.
     *
     * @param dataClass class of for the entity data
     *
     * @return this builder for chaining
     */
    Requirements<TResources, TEntityData, TEvents> entityData(Class<TEntityData> dataClass);

    /**
     * Registers the used concrete resource class for the system. Class must be a record and its record-components must
     * be valid, registered ECS resources.
     *
     * @param resourcesClass class of for the resource data
     *
     * @return this builder for chaining
     */
    Requirements<TResources, TEntityData, TEvents> resources(Class<TResources> resourcesClass);

    /**
     * Registers the used concrete event data class for the system. Class must be a record and its record-components
     * must be valid system events.
     *
     * @param eventsClass class of for the event data
     *
     * @return this builder for chaining
     */
    Requirements<TResources, TEntityData, TEvents> events(Class<TEvents> eventsClass);
}
