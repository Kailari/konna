package fi.jakojaannos.roguelite.game.weapons;

import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.roguelite.game.weapons.WeaponModule;

@SuppressWarnings("rawtypes")
public class WeaponAttributes {
    private final Map<Class<? extends WeaponModule>, Object> attributes = new HashMap<>();

    @SuppressWarnings("unchecked")
    public <TState> TState get(final Class<? extends WeaponModule<?, TState>> moduleClass) {
        if (!this.attributes.containsKey(moduleClass)) {
            throw new IllegalStateException("Tried to get attributes for weapon module, but could not find any "
                                            + "registered. Module: " + moduleClass.getSimpleName());
        }

        // SAFETY: This is safe as long as `put` guarantees matching signatures
        return (TState) this.attributes.get(moduleClass);
    }

    public <TState> void put(
            final Class<? extends WeaponModule<?, TState>> moduleClass,
            final TState attributes
    ) {
        this.attributes.put(moduleClass, attributes);
    }
}
