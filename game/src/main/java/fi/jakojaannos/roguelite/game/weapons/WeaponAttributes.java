package fi.jakojaannos.roguelite.game.weapons;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class WeaponAttributes {
    private final Map<Class<? extends WeaponModule>, Object> attrMap = new HashMap<>();

    public <TState> TState getOrCreateAttributes(
            final Class<? extends WeaponModule<?, TState>> moduleClass,
            final Supplier<TState> attributeConstructor
    ) {
        return (TState) this.attrMap.computeIfAbsent(moduleClass, ignored -> attributeConstructor.get());
    }

    public <TState> void createAttributes(
            final Class<? extends WeaponModule<?, TState>> moduleClass,
            final TState attributes
    ) {
        this.attrMap.put(moduleClass, attributes);
    }
}
