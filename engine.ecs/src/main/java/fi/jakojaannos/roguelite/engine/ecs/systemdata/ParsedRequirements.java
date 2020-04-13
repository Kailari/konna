package fi.jakojaannos.roguelite.engine.ecs.systemdata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;

public record ParsedRequirements<TResources, TEntityData, TEvents>(
        String systemName,
        SystemInputRecord.Resources<TResources>resources,
        SystemInputRecord.EntityData<TEntityData>entityData,
        SystemInputRecord.Events<TEvents>events
) {
    private static final Logger LOG = LoggerFactory.getLogger(EcsSystem.class);

    public TEntityData constructEntityData(final Object[] entityData) {
        return safeNewInputRecordInstance(this.systemName,
                                          "entity data",
                                          this.entityData.constructor(),
                                          entityData);
    }

    public TResources constructResources(final Object[] resources) {
        return safeNewInputRecordInstance(this.systemName,
                                          "resources",
                                          this.resources.constructor(),
                                          resources);
    }

    @Nullable
    public TEvents constructEvents(final Map<Class<?>, Object> eventLookup) {
        final var eventTypes = this.events.componentTypes();
        final var params = new Object[eventTypes.length];
        for (int i = 0; i < params.length; ++i) {
            params[i] = eventLookup.get(eventTypes[i]);

            // Allow non-required events to be null
            final var isRequiredEvent = !this.events.enableOn()[i] && !this.events.disableOn()[i];
            if (params[i] == null && isRequiredEvent) {
                return null;
            }
        }

        return safeNewInputRecordInstance(this.systemName,
                                          "events",
                                          this.events.constructor(),
                                          params);
    }

    private static <T> T safeNewInputRecordInstance(
            final String systemName,
            final String type,
            final Constructor<T> constructor,
            final Object... params
    ) {
        try {
            return constructor.newInstance(params);
        } catch (final InstantiationException e) {
            throw new IllegalStateException(type + " input record cannot be instantiated! ("
                                            + systemName + ")");
        } catch (final IllegalAccessException e) {
            LOG.error("""
                                            
                      ========================================================================================
                      Ticking system {} failed due to {} input record being inaccessible to reflective access!
                       
                      Usually this is caused by module misconfiguration and can be fixed by adding a line with
                         opens <system.package.here> to roguelite.engine.ecs

                      to the "module-info.java" for the module with the system implementation. This allows the
                      ECS requirements handler to reflectively gain access the systems' input types without
                      system implementation needing to provide the constructors manually. Note that the "opens"
                      line in the module info is required on per-package basis.

                      ========================================================================================
                      """,
                      systemName,
                      type);
            throw new IllegalStateException("Cannot access " + type + " input record constructor! ("
                                            + systemName + ") See log above for additional details.");
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(type + " input constructor for system " + systemName
                                            + " failed with an exception: " + e.getMessage());
        }
    }
}