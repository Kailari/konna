package fi.jakojaannos.roguelite.game.weapons;

public interface WeaponModule<TState, TAttributes> {
    TState getDefaultState(TAttributes attributes);

    void register(WeaponHooks hooks);

    /**
     * The default should work as long as the modules are not subclassed. If you are doing wacky stuff and extending
     * modules for some reason and this breaks, just override and provide the correct module class to the state getter.
     *
     * @param state weapon state container to fetch the state from
     *
     * @return the attributes for this weapon module
     */
    @SuppressWarnings("unchecked")
    default TState getState(final WeaponState state, final TAttributes attributes) {
        return state.getOrCreateState((Class<WeaponModule<TState, TAttributes>>) getClass(), () -> this.getDefaultState(attributes));
    }

    /**
     * The default should work as long as the modules are not subclassed. If you are doing wacky stuff and extending
     * modules for some reason and this breaks, just override and provide the correct module class to the attribute
     * getter.
     *
     * @param attributes weapon attributes to fetch the attributes from
     *
     * @return the attributes for this weapon module
     */
    @SuppressWarnings("unchecked")
    default TAttributes getAttributes(final WeaponAttributes attributes) {
        return attributes.get((Class<WeaponModule<TState, TAttributes>>) getClass());
    }
}
