package fi.jakojaannos.riista.ecs;

import java.util.Collection;

/**
 * Logical group of systems with internal ordering. Systems inside a group are executed sequentially.
 */
public interface SystemGroup {
    /**
     * Gets group dependencies for this system group. This is a collection of all systems which must be ticked before
     * this group may tick. If any of the dependencies is disabled or cannot be ticked, this group may not tick either.
     *
     * @return system group dependencies
     */
    Collection<SystemGroup> getDependencies();

    /**
     * Gets all the systems registered to this group.
     *
     * @return systems in this group
     */
    Collection<EcsSystem<?, ?, ?>> getSystems();

    /**
     * Gets the name of this system group.
     *
     * @return the name of this group
     */
    String getName();

    /**
     * Should this group enabled by default? This is merely a hint for dispatcher/whoever builds the {@link SystemState}
     * for this group whether or not the group should be enabled, thus this is not a guarantee that the system will be
     * enabled/disabled on first tick.
     *
     * @return should this group be enabled by default?
     */
    boolean isEnabledByDefault();

    /**
     * A builder for constructing system groups. Allows registering systems and group dependencies. Group builders
     * created via {@link SystemDispatcher.Builder dispatcher builder} are registered to that dispatch builder by
     * default and need not be further registered anywhere.
     *
     * @see SystemDispatcher.Builder#group(String)
     */
    interface Builder {
        /**
         * Builds this group and constructs a handle to it. Use the handle to register other groups' dependencies.
         *
         * @return a system group handle for this group
         */
        SystemGroup buildGroup();

        /**
         * Registers a system to this group. Registration order determines the execution order. All systems in a group
         * run sequentially, in the order they are registered.
         *
         * @param system system to register
         *
         * @return this builder for chaining
         */
        Builder withSystem(EcsSystem<?, ?, ?> system);

        /**
         * Registers a dependency on given system group(s). All these dependencies must be ticked before this system
         * group may tick. If any of the dependencies is disabled or for any other reason cannot tick, this group may
         * not tick either.
         *
         * @param dependencies system group dependencies
         *
         * @return this builder for chaining
         */
        Builder dependsOn(SystemGroup... dependencies);

        /**
         * Marks this system group to be disabled by default.
         *
         * @return this builder for chaining
         */
        Builder disabledByDefault();
    }
}
