package fi.jakojaannos.roguelite.engine.ecs.newimpl;

import java.lang.reflect.Constructor;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.systemdata.RequirementsBuilderImpl;

public interface Requirements<TResources, TEntityData, TEvents> {
    static <TResources, TEntityData, TEvents> Builder<TResources, TEntityData, TEvents> builder() {
        return new RequirementsBuilderImpl<>();
    }

    Constructor<TEntityData> entityDataConstructor();

    Class<?>[] entityDataComponentTypes();

    Constructor<TResources> resourceConstructor();

    Class<?>[] resourceComponentTypes();

    interface Builder<TResources, TEntityData, TEvents> {
        Builder<TResources, TEntityData, TEvents> entityData(Class<TEntityData> dataClass);

        Builder<TResources, TEntityData, TEvents> resources(Class<TResources> resourcesClass);

        Builder<TResources, TEntityData, TEvents> events(Class<TEvents> eventsClass);

        Requirements<TResources, TEntityData, TEvents> build();
    }
}
