package fi.jakojaannos.roguelite.game.weapons;

import java.util.Map;

record WeaponInstanceImpl(
        Map<Class<?>, Object>states,
        Map<Class<?>, Object>attributes
) implements Weapon {
    @Override
    @SuppressWarnings("unchecked")
    public <TState> TState getState(final Class<TState> stateClass) {
        if (!this.states.containsKey(stateClass)) {
            throw new IllegalStateException("State not present: " + stateClass.getSimpleName());
        }
        return (TState) this.states.get(stateClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TAttributes> TAttributes getAttributes(final Class<TAttributes> attributesClass) {
        if (!this.attributes.containsKey(attributesClass)) {
            throw new IllegalStateException("Attributes not present: " + attributesClass.getSimpleName());
        }
        return (TAttributes) this.attributes.get(attributesClass);
    }
}
