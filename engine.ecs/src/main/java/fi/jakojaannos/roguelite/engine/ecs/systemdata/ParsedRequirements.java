package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.world.storage.ResourceStorage;

public record ParsedRequirements<TResources, TEntityData, TEvents>(
        SystemInputRecord.EntityData<TEntityData>entityData,
        SystemInputRecord.Resources<TResources>resources,
        SystemInputRecord.Events<TEvents>events
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
        } catch (final InstantiationException e) {
            throw new IllegalStateException("Resources input cannot be instantiated!");
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Cannot access resource input constructor! ("
                                            + this.resources.constructor().getDeclaringClass().getName()
                                            + ")");
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("Resource input constructor failed: " + e.getMessage());
        }
    }

    @Nullable
    public TEvents constructEvents(final Map<Class<?>, Object> eventLookup) {
        final var eventTypes = this.events.componentTypes();
        final var params = new Object[eventTypes.length];
        for (int i = 0; i < params.length; ++i) {
            params[i] = eventLookup.get(eventTypes[i]);

            final var isRequiredEvent = !this.events.enableOn()[i] && !this.events.disableOn()[i];
            if (params[i] == null && isRequiredEvent) {
                return null;
            }
        }

        try {
            return this.events.constructor().newInstance(params);
        } catch (final InstantiationException e) {
            throw new IllegalStateException("Resources input cannot be instantiated!");
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException("Cannot access resource input constructor! ("
                                            + this.resources.constructor().getDeclaringClass().getName()
                                            + ")");
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException("Resource input constructor failed: " + e.getMessage());
        }
    }
}