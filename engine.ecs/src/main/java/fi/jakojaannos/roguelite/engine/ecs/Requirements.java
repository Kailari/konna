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
    Requirements<TResources, TEntityData, TEvents> entityData(Class<TEntityData> dataClass);

    Requirements<TResources, TEntityData, TEvents> resources(Class<TResources> resourcesClass);

    Requirements<TResources, TEntityData, TEvents> events(Class<TEvents> eventsClass);
}
