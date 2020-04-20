package fi.jakojaannos.roguelite.game.weapons;

/**
 * Weapon instance. Holds state instances and attribute references for the weapon.
 */
public interface Weapon {
    /**
     * Fetches a state for this weapon. Assumes the state exists. If no module which has registered state with given
     * class is present, the operation will fail.
     *
     * @param stateClass class of the state to get
     * @param <TState>   type of the state to get
     *
     * @return the state instance
     */
    <TState> TState getState(Class<TState> stateClass);

    /**
     * Fetches attributes for this weapon. Assumes the attributes exists. If no module which has registered attributes
     * with given class is present, the operation will fail.
     *
     * @param attributesClass class of the attributes to get
     * @param <TAttributes>>  type of the attributes to get
     *
     * @return the state instance
     */
    <TAttributes> TAttributes getAttributes(Class<TAttributes> attributesClass);
}
