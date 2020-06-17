package fi.jakojaannos.riista.ecs.resources;

import java.util.function.Function;
import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.EntityHandle;

public interface Entities {
    EntityHandle createEntity(Object... components);

    /**
     * Creates a spliterator for iterating over the entities with specified components pre-fetched.
     * <p>
     * <strong>
     * USE OF THIS METHOD IS HEAVILY DISCOURAGED AND GENERALLY SHOULD BE AVOIDED
     * </strong>
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
            Function<Object[], TEntityData> dataFactory,
            boolean parallel
    );
}
