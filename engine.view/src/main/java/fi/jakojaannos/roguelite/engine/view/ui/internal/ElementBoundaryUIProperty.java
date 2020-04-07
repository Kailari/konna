package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
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

    private static <T> Function<EntityHandle, Optional<T>> createGetter(
            final Function<ElementBoundaries, T> boundsToValueMapper
    ) {
        return (entity) -> entity.getComponent(ElementBoundaries.class)
                                 .map(boundsToValueMapper);
    }

    private static <C extends ProportionValueComponent> ValueSetter<Integer> createSetter(
            final Class<C> componentClass,
            final Function<ProportionValue, C> componentFactory,
            final BiConsumer<ElementBoundaries, Integer> boundsValueSetter
    ) {
        return (entity, value) -> {
            boundsValueSetter.accept(entity.addOrGet(ElementBoundaries.class, ElementBoundaries::new), value);
            entity.getComponent(componentClass)
                  .ifPresentOrElse(backingComponent -> backingComponent.setValue(ProportionValue.absolute(value)),
                                   () -> entity.addComponent(componentFactory.apply(ProportionValue.absolute(value))));
        };
    }
}
