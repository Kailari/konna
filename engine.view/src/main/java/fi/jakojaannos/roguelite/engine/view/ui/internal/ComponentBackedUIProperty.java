package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

public class ComponentBackedUIProperty<T, C> extends EntityBackedUIProperty<T> {
    public ComponentBackedUIProperty(
            final String name,
            final Class<C> componentClass,
            final Function<C, T> componentToValueMapper,
            final BiConsumer<C, T> componentValueSetter
    ) {
        super(name,
              createGetter(componentClass, componentToValueMapper),
              createSetter(componentClass, componentValueSetter));
    }

    private static <T, C> Function<EntityHandle, Optional<T>> createGetter(
            final Class<C> componentClass,
            final Function<C, T> componentToValueMapper
    ) {
        return (entityHandle) -> entityHandle.getComponent(componentClass)
                                             .map(componentToValueMapper);
    }

    private static <T, C> ValueSetter<T> createSetter(
            final Class<C> componentClass,
            final BiConsumer<C, T> componentValueSetter
    ) {
        return (entity, value) ->
                entity.getComponent(componentClass)
                      .ifPresent(component -> componentValueSetter.accept(component, value));
    }
}
