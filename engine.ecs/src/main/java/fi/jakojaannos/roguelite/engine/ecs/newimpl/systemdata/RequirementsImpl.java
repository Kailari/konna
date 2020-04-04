package fi.jakojaannos.roguelite.engine.ecs.newimpl.systemdata;

import java.lang.reflect.Constructor;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.Requirements;

public record RequirementsImpl<TResources, TEntityData, TEvents>(
        SystemInputRecord<TEntityData>entityData,
        SystemInputRecord<TResources>resources
) implements Requirements<TResources, TEntityData, TEvents> {
    @Override
    public Constructor<TEntityData> entityDataConstructor() {
        return this.entityData.constructor();
    }

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
}
