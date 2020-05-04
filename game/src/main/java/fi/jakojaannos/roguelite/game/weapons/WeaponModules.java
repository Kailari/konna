package fi.jakojaannos.roguelite.game.weapons;

import java.util.Map;
import java.util.Optional;

public class WeaponModules {
    private final Map<Class<?>, Object> modules;

    public WeaponModules(final Map<Class<?>, Object> modules) {
        this.modules = modules;
    }

    @SuppressWarnings("unchecked")
    public <TModule> TModule require(final Class<TModule> moduleClass) {
        if (!this.modules.containsKey(moduleClass)) {
            throw new IllegalStateException("Module not present: " + moduleClass.getSimpleName());
        }
        return (TModule) this.modules.get(moduleClass);
    }

    @SuppressWarnings("unchecked")
    public <TModule> Optional<TModule> get(final Class<TModule> moduleClass) {
        return Optional.ofNullable((TModule) this.modules.get(moduleClass));
    }
}
