package fi.jakojaannos.roguelite.engine.ecs;

import java.util.stream.Stream;

/**
 * A single stateless unit for data manipulation. Accepts data with specified shape and applies some
 * transformation/mutation over it.
 * <p>
 * Specifying resources makes this system depend on said resources. E.g. the resources must be available in order for
 * this system to be ticked. If no resources are needed use {@link EcsSystem.NoResources}.
 * <p>
 * Specifying entities makes this system require matching entities. There must be at least single entity matching the
 * requirements in order for this system to be ticked. If no entities are needed use {@link EcsSystem.NoEntities}. If
 * system for some reason requires iterating through all entities, {@link EcsSystem.AllEntities} can be used. The latter
 * is not advised and should be avoided, where possible.
 * <p>
 * Specifying events makes this system depend on said events. This causes the system to tick only when matching events
 * are fired. If no events are needed, use {@link EcsSystem.NoEvents}.
 *
 * @param <TResources>  container class for holding the required resources
 * @param <TEntityData> container class for holding components of required entities
 * @param <TEvents>     container class for holding instances of required events
 */
public interface EcsSystem<TResources, TEntityData, TEvents> {

    /**
     * Runs a single simulation tick for this system. Performs any data manipulation the system is intended to do.
     *
     * @param resources input resources
     * @param entities  stream of input entities
     * @param events    input events
     */
    void tick(
            TResources resources,
            Stream<EntityDataHandle<TEntityData>> entities,
            TEvents events
    );

    /**
     * Utility tag for creating systems without resource requirements. Substitute this for <code>TResources</code> to
     * inform the dispatcher that this system requires no resources.
     */
    record NoResources() {
    }

    /**
     * Utility tag for creating systems without entity requirements. Substitute this for <code>TEntities</code> to
     * inform the dispatcher that this system requires no entities.
     */
    record NoEntities() {
    }

    /**
     * Utility tag for creating systems without event requirements. Substitute this for <code>TEvents</code> to inform
     * the dispatcher that this system requires no events.
     */
    record NoEvents() {
    }

    /**
     * Tag for iterating all entities. This does not give any guarantees about which components the entities being read
     * have.
     */
    record AllEntities() {
    }
}
