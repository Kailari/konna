package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.Optional;
import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.UIProperty;

public class EntityBackedUIProperty<T> implements UIProperty<T> {
    private final String name;
    private final Function<EntityHandle, Optional<T>> valueGetter;
    private final ValueSetter<T> valueSetter;

    @Override
    public String getName() {
        return this.name;
    }

    public EntityBackedUIProperty(
            final String name,
            final Function<EntityHandle, Optional<T>> valueGetter,
            final ValueSetter<T> valueSetter
    ) {
        this.name = name;
        this.valueGetter = valueGetter;
        this.valueSetter = valueSetter;
    }

    @Override
    public Optional<T> getFor(final UIElement uiElement) {
        if (uiElement instanceof EntityBackedUIElement entityBackedUiElement) {
            return this.valueGetter.apply(entityBackedUiElement.getEntity());
        }

        throw new IllegalStateException("Unknown type of UI Element: \"" +
                                        uiElement.getClass().getSimpleName() + "\"");
    }

    @Override
    public void set(final UIElement uiElement, final T value) {
        if (uiElement instanceof EntityBackedUIElement entityBackedUiElement) {
            this.valueSetter.accept(entityBackedUiElement.getEntity(), value);
            return;
        }

        throw new IllegalStateException("Unknown type of UI Element: \"" +
                                        uiElement.getClass().getSimpleName() + "\"");
    }

    public interface ValueSetter<T> {
        void accept(EntityHandle entity, T value);
    }
}
