package fi.jakojaannos.roguelite.game.weapons;

import java.util.Map;
import java.util.Optional;

public class WeaponModules {
    private final Map<Class<?>, Object> modules;

    /**
     * @param modules map of weapon's module classes and instance of that module
     */
    public WeaponModules(final Map<Class<?>, Object> modules) {
        this.modules = modules;
    }

    /**
     * Gets the module of given class from the weapon the module is attached to
     *
     * @param moduleClass class of the module
     * @param <TModule>   class of the module
     *
     * @return module of given class
     *
     * @throws IllegalStateException if the module is not present in weapon
     */
    @SuppressWarnings("unchecked")
    public <TModule> TModule require(final Class<TModule> moduleClass) {
        if (!this.modules.containsKey(moduleClass)) {
            throw new IllegalStateException("Module not present: " + moduleClass.getSimpleName());
        }
        return (TModule) this.modules.get(moduleClass);
    }

    /**
     * Gets the module of given class from the weapon if it's present
     *
     * @param moduleClass class of the module
     * @param <TModule>   class of the module
     *
     * @return module of given class if it's present, otherwise {@code Optional.empty}
     */
    @SuppressWarnings("unchecked")
    public <TModule> Optional<TModule> get(final Class<TModule> moduleClass) {
        return Optional.ofNullable((TModule) this.modules.get(moduleClass));
    }
}
