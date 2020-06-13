package fi.jakojaannos.roguelite.engine.ecs;

import java.util.Collection;

import fi.jakojaannos.roguelite.engine.ecs.dispatcher.SystemDispatcherBuilderImpl;

/**
 * A system dispatcher for ticking a collection of systems in a controlled manner. Handles execution order based on
 * defined dependencies and constraints.
 */
public interface SystemDispatcher extends AutoCloseable {
    Collection<Class<?>> getSystems();

    Collection<SystemGroup> getGroups();

    /**
     * Sets the per-system parallelism on/off. Defaults to <code>true</code>. When enabled, the entity stream passed to
     * systems is evaluated with multiple worker threads. Individual systems are still executed sequentially.
     *
     * @param state whether or not to enable parallel entity stream
     */
    void setParallel(boolean state);

    /**
     * Creates a new dispatcher builder for creating a system dispatcher.
     *
     * @return the builder
     */
    static Builder builder() {
        return new SystemDispatcherBuilderImpl();
    }

    /**
     * Constructs a default state for this dispatcher. Generally this enables all systems by default and disables
     * everything with the {@link fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault @DisabledByDefault}
     * annotation.
     *
     * @return a default {@link SystemState} for this dispatcher
     */
    SystemState createDefaultState();

    /**
     * Ticks the registered systems. System enabled/disabled status is determined by the <code>systemState</code> and
     * events from <code>systemEvents</code> are propagated down to systems, where appropriate.
     *
     * @param world        world to manipulate
     * @param systemState  state of the systems
     * @param systemEvents present events
     */
    void tick(World world, SystemState systemState, Collection<Object> systemEvents);

    /**
     * Ticks the registered systems, with everything defined to be enabled by default enabled. Events are not
     * processed.
     *
     * @param world        world to manipulate
     * @param systemEvents
     */
    void tick(World world, final Collection<Object> systemEvents);

    @Override
    void close();

    /**
     * System dispatcher builder, for registering systems and groups for the dispatcher.
     */
    interface Builder {
        /**
         * Builds the dispatcher.
         *
         * @return the dispatcher
         */
        SystemDispatcher build();

        /**
         * Adds a new system group to the dispatcher. A system group is required for registering individual systems as
         * each system <strong>must</strong> belong to a group.
         *
         * @param name name of the group
         *
         * @return this builder for chaining.
         */
        SystemGroup.Builder group(String name);
    }
}
