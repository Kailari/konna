package fi.jakojaannos.roguelite.engine.ecs.newecs.systemdata;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import fi.jakojaannos.roguelite.engine.ecs.newecs.Requirements;

public record RequirementsImpl<TResources, TEntityData, TEvents>(
        SystemInputRecord<TEntityData>entityData,
        SystemInputRecord<TResources>resources
) implements Requirements<TResources, TEntityData, TEvents> {
    @Override
    public Class<?>[] entityDataComponentTypes() {
        return this.entityData.componentTypes();
    }

    @Override
    public Constructor<TResources> resourceConstructor() {
        return this.resources.constructor();
    }

    @Override
    public Class<?>[] resourceComponentTypes() {
        return this.resources.componentTypes();
    }

    @Override
    public TEntityData entityDataFactory(final Object[] params) {
        try {
            return this.entityData.constructor().newInstance(params);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not instantiate entity data!");
        }
    }
}
