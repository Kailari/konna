package fi.jakojaannos.roguelite.engine.ecs.newimpl;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * A single stateless unit for data manipulation. Accepts data with specified shape and applies some
 * transformation/mutation over it.
 * <p>
 * Specifying resources makes this system depend on said resources. E.g. the resources must be available in order for
 * this system to be ticked. If no resources are needed use {@link EcsSystem.NoResources}.
 * <p>
 * Specifying entities makes this system require matching entities. There must be at least single entity matching the
 * requirements in order for this system to be ticked. If no entities are needed use {@link EcsSystem.NoEntities}.
 * <p>
 * Specifying events makes this system depend on said events. This causes the system to tick only when matching events
 * are fired. If no events are needed, use {@link EcsSystem.NoEvents}.
 *
 * @param <TResources>  container class for holding the required resources
 * @param <TEntityData> container class for holding components of required entities
 * @param <TEvents>     container class for holding instances of required events
 */
public interface EcsSystem<TResources, TEntityData, TEvents> {
    default Requirements<TResources, TEntityData, TEvents> declareRequirements() {
        return declareRequirements(Requirements.builder());
    }

    Requirements<TResources, TEntityData, TEvents> declareRequirements(
            Requirements.Builder<TResources, TEntityData, TEvents> require
    );

    void tick(
            TResources resources,
            Stream<EntityDataHandle<TEntityData>> entities,
            TEvents events
    );

    interface EntityDataHandle<TEntityData> {
        TEntityData getData();

        /**
         * @see EntityHandle#isPendingRemoval()
         */
        boolean isPendingRemoval();

        /**
         * @see EntityHandle#getId()
         */
        int getId();

        EntityHandle getHandle();

        /**
         * @see EntityHandle#addComponent(Object)
         */
        <TComponent> boolean addComponent(TComponent component);

        /**
         * @see EntityHandle#removeComponent(Class)
         */
        <TComponent> boolean removeComponent(Class<TComponent> componentClass);

        /**
         * @see EntityHandle#hasComponent(Class)
         */
        <TComponent> boolean hasComponent(Class<TComponent> componentClass);

        /**
         * @see EntityHandle#getComponent(Class)
         */
        <TComponent> Optional<TComponent> getComponent(Class<TComponent> componentClass);

        /**
         * @see EntityHandle#destroy()
         */
        void destroy();
    }

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
}
