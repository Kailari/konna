package fi.jakojaannos.roguelite.engine.ecs.newimpl;

import java.util.function.Consumer;
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
@SuppressWarnings("unused")
public interface EcsSystem<TResources, TEntityData, TEvents> {
    Requirements<TEntityData> declareRequirements();

    void tick(
            TResources resources,
            Stream<EntityDataHandle<TEntityData>> entities,
            TEvents events
    );

    interface EntityDataHandle<TEntityData> extends EntityHandle {
        TEntityData getData();

        int getId();
    }

    /**
     * Utility tag for creating systems without resource requirements. Substitute this for <code>TResources</code> to
     * inform the dispatcher that this system requires no resources.
     */
    final class NoResources {
    }

    /**
     * Utility tag for creating systems without entity requirements. Substitute this for <code>TEntities</code> to
     * inform the dispatcher that this system requires no entities.
     */
    final class NoEntities {
    }

    /**
     * Utility tag for creating systems without event requirements. Substitute this for <code>TEvents</code> to inform
     * the dispatcher that this system requires no events.
     */
    final class NoEvents {
    }
}
