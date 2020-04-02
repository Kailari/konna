package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.Optional;
import java.util.function.BiFunction;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

public class EntityBackedUIProperty<T> implements UIProperty<T> {
    private final String name;
    private final BiFunction<Entity, EntityManager, Optional<T>> valueGetter;
    private final ValueSetter<T> valueSetter;

    @Override
    public String getName() {
        return this.name;
    }

    public EntityBackedUIProperty(
            final String name,
            final BiFunction<Entity, EntityManager, Optional<T>> valueGetter,
            final ValueSetter<T> valueSetter
    ) {
        this.name = name;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    @Override
    public Optional<T> getFor(final UIElement uiElement) {
        if (uiElement instanceof EntityBackedUIElement entityBackedUiElement) {
            return this.valueGetter.apply(entityBackedUiElement.getEntity(),
                                          entityBackedUiElement.getEntityManager());
        }

        throw new IllegalStateException("Unknown type of UI Element: \"" +
                                                uiElement.getClass().getSimpleName() + "\"");
    }

    @Override
    public void set(final UIElement uiElement, final T value) {
        if (uiElement instanceof EntityBackedUIElement entityBackedUiElement) {
            this.valueSetter.accept(entityBackedUiElement.getEntity(),
                                    entityBackedUiElement.getEntityManager(),
                                    value);
            return;
        }

        throw new IllegalStateException("Unknown type of UI Element: \"" +
                                                uiElement.getClass().getSimpleName() + "\"");
    }

    public interface ValueSetter<T> {
        void accept(Entity entity, EntityManager entityManager, T value);
    }
}
