package fi.jakojaannos.roguelite.engine.view.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public class EntityBackedUIProperty<T> implements UIProperty<T> {
    private final String name;
    private final BiFunction<Entity, EntityManager, Optional<T>> valueGetter;
    private final ValueSetter<T> valueSetter;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Optional<T> getFor(final UIElement uiElement) {
        if (!(uiElement instanceof EntityBackedUIElement)) {
            throw new IllegalStateException("Unknown type of UI Element: \"" + uiElement.getClass().getSimpleName() + "\"");
        }
        val entityBackedUiElement = (EntityBackedUIElement) uiElement;
        return this.valueGetter.apply(entityBackedUiElement.getEntity(),
                                      entityBackedUiElement.getEntityManager());
    }

    @Override
    public void set(final UIElement uiElement, final T value) {
        if (!(uiElement instanceof EntityBackedUIElement)) {
            throw new IllegalStateException("Unknown type of UI Element: \"" + uiElement.getClass().getSimpleName() + "\"");
        }
        val entityBackedUiElement = (EntityBackedUIElement) uiElement;
        this.valueSetter.accept(entityBackedUiElement.getEntity(),
                                entityBackedUiElement.getEntityManager(),
                                value);
    }

    public interface ValueSetter<T> {
        void accept(Entity entity, EntityManager entityManager, T value);
    }
}
