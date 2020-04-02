package fi.jakojaannos.roguelite.engine.ecs.newimpl;

import java.lang.reflect.Constructor;

public interface Requirements<TEntityData> {
    void entityData(Class<TEntityData> dataClass);

    Constructor<TEntityData> getConstructor();

    Class<?>[] getComponentTypes();
}
