package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import java.lang.reflect.InvocationTargetException;

import fi.jakojaannos.roguelite.engine.ecs.world.storage.ResourceStorage;

public record ParsedRequirements<TResources, TEntityData, TEvents>(
        SystemInputRecord<TEntityData>entityData,
        SystemInputRecord<TResources>resources
) {
    public TEntityData constructEntityData(final Object[] params) {
        try {
            return this.entityData.constructor().newInstance(params);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not instantiate entity data!");
        }
    }

    public TResources constructResources(final ResourceStorage resourceStorage) {
        try {
            final var resources = resourceStorage.fetch(this.resources.componentTypes());
            return this.resources.constructor().newInstance(resources);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not instantiate resources!");
        }
    }
}