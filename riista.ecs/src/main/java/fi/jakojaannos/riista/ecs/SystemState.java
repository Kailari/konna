package fi.jakojaannos.riista.ecs;

import java.util.Collection;

/**
 * State of the systems. Handles enabling/disabling systems and groups and querying their respective states.
 */
public interface SystemState {
    /**
     * Queries the state for whether or not a given system is currently enabled. Note that this may return true even if
     * the group the system belongs to is disabled, meaning that this is not a guarantee the system will be ticked.
     *
     * @param systemClass class of the system to check
     *
     * @return <code>true</code> if the system is enabled
     */
    boolean isEnabled(Class<?> systemClass);

    /**
     * Queries the state for whether or not a given system group is currently enabled.
     *
     * @param systemGroup group which state to query
     *
     * @return <code>true</code> if the system group is enabled
     */
    boolean isEnabled(SystemGroup systemGroup);

    /**
     * Sets the state of the system with given class.
     *
     * @param systemClass system which state to set
     * @param state       new state
     */
    void setState(Class<?> systemClass, boolean state);

    /**
     * Sets the state of a system group.
     *
     * @param systemGroup group which state to set
     * @param state       new state
     */
    void setState(SystemGroup systemGroup, boolean state);

    void resetToDefaultState(Collection<Class<?>> systems);

    void resetGroupsToDefaultState(Collection<SystemGroup> systemGroups);
}
