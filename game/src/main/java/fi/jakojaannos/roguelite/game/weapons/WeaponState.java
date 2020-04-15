package fi.jakojaannos.roguelite.game.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WeaponState {
    private final Map<Class<? extends WeaponModule>, Object> stateMap = new HashMap<>();

    public <TState> TState getOrCreateState(
            final Class<? extends WeaponModule<TState, ?>> moduleClass,
            final Supplier<TState> stateConstructor
    ) {
        return (TState) this.stateMap.computeIfAbsent(moduleClass, ignored -> stateConstructor.get());
    }
}
