package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.view.data.components.internal.ProportionValueComponent;
import fi.jakojaannos.roguelite.engine.view.data.components.ui.ElementBoundaries;
import fi.jakojaannos.roguelite.engine.view.ui.ProportionValue;

public class ElementBoundaryUIProperty<C extends ProportionValueComponent>
        extends EntityBackedUIProperty<Integer> {

    public ElementBoundaryUIProperty(
            final String name,
            final Class<C> componentClass,
            final Function<ProportionValue, C> componentSupplier,
            final Function<ElementBoundaries, Integer> boundsToValueMapper,
            final BiConsumer<ElementBoundaries, Integer> boundsValueSetter
    ) {
        super(name,
              createGetter(boundsToValueMapper),
              createSetter(componentClass, componentSupplier, boundsValueSetter));
    }

    private static <T> BiFunction<Entity, EntityManager, Optional<T>> createGetter(
            final Function<ElementBoundaries, T> boundsToValueMapper
    ) {
        return (entity, entityManager) -> entityManager.getComponentOf(entity, ElementBoundaries.class)
                                                       .map(boundsToValueMapper);
    }

    private static <C extends ProportionValueComponent> ValueSetter<Integer> createSetter(
            final Class<C> componentClass,
            final Function<ProportionValue, C> componentFactory,
            final BiConsumer<ElementBoundaries, Integer> boundsValueSetter
    ) {
        return (entity, entityManager, value) -> {
            boundsValueSetter.accept(entityManager.addComponentIfAbsent(entity,
                                                                        ElementBoundaries.class,
                                                                        ElementBoundaries::new),
                                     value);
            entityManager.getComponentOf(entity, componentClass)
                         .ifPresentOrElse(backingComponent ->
                                                  backingComponent.setValue(ProportionValue.absolute(value)),
                                          () -> entityManager.addComponentTo(entity,
                                                                             componentFactory.apply(
                                                                                     ProportionValue.absolute(value)))
                         );
        };
    }
}
