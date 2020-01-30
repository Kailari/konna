package fi.jakojaannos.roguelite.engine.ecs;

import java.util.stream.Stream;

/**
 * A single stateless logical unit, performing a data transformation procedure on collections of {@link Component
 * components} based on groupings called {@link Entity entities}. Provides a {@link #tick(Stream, World) tick}-method
 * for applying the state update on compatible entities.
 * <p>
 * Systems should never be manually ticked outside of a test environment. Instead, a {@link SystemDispatcher dispatcher}
 * should be used, as it automatically handles managing dependencies and resource requirements. All system instances
 * must be registered when constructing the
 * <code>SystemDispatcher</code> with the {@link SystemDispatcher#builder() Dispatcher builder}.
 * <p>
 * Systems are stateless. Having any persistent per-world state-full data on systems is strictly prohibited. If you need
 * cached results for an expensive calculation, use a {@link Resource}. It is allowed, however, to have fields for
 * temporary variables to avoid constant allocations. Such variables should be named accordingly, and current convention
 * is to prefix those with "tmp". For example, system which often calculates some direction vector, could have field
 * <code>private final Vector2d tmpSomeDirection</code>, which is then recycled. Care must be taken for recycled
 * objects
 * old state not accidentally affecting other entities.
 * <p>
 * System dependencies are defined in {@link #declareRequirements(RequirementsBuilder)} via a {@link
 * RequirementsBuilder}. Typically, systems should be designed so that execution order does not matter. However, in
 * order to achieve deterministic, predictable behavior, it is sometimes necessary to give the dispatcher hints on the
 * order in which the systems should be executed. For these cases, {@link SystemGroup groups} and a number of {@link
 * RequirementsBuilder#tickAfter(Class) methods} {@link RequirementsBuilder#tickAfter(SystemGroup) for} {@link
 * RequirementsBuilder#tickBefore(Class) controlling} {@link RequirementsBuilder#tickBefore(SystemGroup) the order} of
 * execution. <strong>These methods are to be used sparingly, as the dependency tree should be kept as shallow as
 * possible.</strong> Defining dependencies is not something that can outweigh clever systems design, but a tool to be
 * used to ensure that resources are updated in consistent order.
 *
 * @see Resource
 * @see Component
 * @see Entity
 * @see SystemGroup
 * @see SystemDispatcher
 */
public interface ECSSystem {
    /**
     * Declares dependency- and resource -requirements for this system. All components and resources required for
     * operation of this system should be marked via the provided {@link RequirementsBuilder}. Additionally, should this
     * system belong to a group and/or depend on execution order with another system, provided builder can manage that,
     * too.
     * <p>
     * Any components marked as required here are guaranteed to exist on entities during {@link #tick(Stream, World)
     * tick}. Same goes the other way around for excluding components. In other words
     * <pre>
     * {@code
     *  // in declareRequirements
     *  requirements.withComponent(MyComponent.class)
     *              .withoutComponent(ExcludedComponent.class);
     *
     *  // in tick
     *  assertTrue(entities.allMatch(entity -> world.getEntityManager()
     *                                              .getComponent(entity, MyComponent.class)
     *                                              .isPresent()));
     *  assertTrue(entities.noneMatch(entity -> world.getEntityManager()
     *                                               .hasComponent(entity, ExcludedComponent.class)));
     * }
     * </pre>
     *
     * @param requirements Requirements builder for declaring the requirements
     */
    void declareRequirements(RequirementsBuilder requirements);

    /**
     * Performs the state manipulation on given {@link World}. All entities are guaranteed to match requirements
     * specified in {@link #declareRequirements(RequirementsBuilder)}.
     *
     * @param entities stream of matching entities to operate on
     * @param world    world the entities belong to
     */
    void tick(Stream<Entity> entities, World world);
}
