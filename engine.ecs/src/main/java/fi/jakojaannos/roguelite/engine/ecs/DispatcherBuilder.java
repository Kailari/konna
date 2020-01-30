package fi.jakojaannos.roguelite.engine.ecs;

/**
 * Constructs a new {@link SystemDispatcher}. Use {@link SystemDispatcher#builder()} to get a new builder instance.
 * <p>
 * All {@link SystemGroup SystemGroups} must be registered before any of the systems can be registered.
 */
public interface DispatcherBuilder {
    /**
     * Registers the system group to this dispatcher.
     *
     * @param group group to register
     *
     * @return the builder for chaining
     */
    DispatcherBuilder withGroup(SystemGroup group);

    /**
     * Convenience method for registering multiple systems at once
     *
     * @param groups groups to register
     *
     * @return the builder for chaining
     */
    default DispatcherBuilder withGroups(SystemGroup... groups) {
        for (final var group : groups) {
            withGroup(group);
        }
        return this;
    }

    /**
     * Adds a new dependency between two {@link SystemGroup SystemGroups}
     *
     * @param group      group to add the dependency to
     * @param dependency the another group the first group depends on
     *
     * @return the builder for chaining
     */
    DispatcherBuilder addGroupDependency(
            SystemGroup group,
            SystemGroup dependency
    );

    /**
     * Convenience method for making a group depend on multiple other groups.
     *
     * @param group        group to add the dependencies to
     * @param dependencies the other groups the first group depends on
     *
     * @return the builder for chaining
     */
    default DispatcherBuilder addGroupDependencies(
            SystemGroup group,
            SystemGroup... dependencies
    ) {
        for (final var dependency : dependencies) {
            addGroupDependency(group, dependency);
        }
        return this;
    }

    /**
     * Registers the system instance to this dispatcher.
     *
     * @param system system instance to add
     *
     * @return the builder for chaining
     */
    DispatcherBuilder withSystem(ECSSystem system);

    /**
     * Registers all listed system instances to this dispatcher.
     *
     * @param systems system instances to add
     *
     * @return the builder for chaining
     */
    default DispatcherBuilder withSystems(ECSSystem... systems) {
        for (final var system : systems) {
            withSystem(system);
        }
        return this;
    }

    /**
     * Finalizes the construction process and outputs a fully operational {@link SystemDispatcher} instance.
     *
     * @return the newly constructed <code>SystemDispatcher</code>
     */
    SystemDispatcher build();
}
