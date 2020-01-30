package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;

public class ComponentBackedUIProperty<T, C extends Component> extends EntityBackedUIProperty<T> {
    public ComponentBackedUIProperty(
            final String name,
            final Class<? extends C> componentClass,
            final Function<C, T> componentToValueMapper,
            final BiConsumer<C, T> componentValueSetter
    ) {
        super(name,
              createGetter(componentClass, componentToValueMapper),
              createSetter(componentClass, componentValueSetter));
    }

    private static <T, C extends Component> BiFunction<Entity, EntityManager, Optional<T>> createGetter(
            final Class<? extends C> componentClass,
            final Function<C, T> componentToValueMapper
    ) {
        return (entity, entityManager) -> entityManager.getComponentOf(entity, componentClass)
                                                       .map(componentToValueMapper);
    }

    private static <T, C extends Component> ValueSetter<T> createSetter(
            final Class<? extends C> componentClass,
            final BiConsumer<C, T> componentValueSetter
    ) {
        return (entity, entityManager, value) ->
                entityManager.getComponentOf(entity, componentClass)
                             .ifPresent(component -> componentValueSetter.accept(component, value));
    }
}
