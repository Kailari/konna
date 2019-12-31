package fi.jakojaannos.roguelite.engine.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ui.UIElement;
import fi.jakojaannos.roguelite.engine.ui.UIProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@RequiredArgsConstructor
public class ComponentBackedUIProperty<T, C extends Component> implements UIProperty<T> {
    @Getter private final String name;
    private final Class<? extends C> componentClass;
    private final Function<C, T> componentToValueMapper;
    private final BiConsumer<C, T> componentValueSetter;

    @Override
    public Optional<T> getFor(final UIElement uiElement) {
        if (!(uiElement instanceof EntityBackedUIElement)) {
            throw new IllegalStateException("Unknown type of UI Element: \"" + uiElement.getClass().getSimpleName() + "\"");
        }
        val entityBackedUiElement = (EntityBackedUIElement) uiElement;
        return getValueFromEntity(entityBackedUiElement.getEntity(),
                                  entityBackedUiElement.getEntityManager());
    }

    @Override
    public void set(final UIElement uiElement, final T value) {
        if (!(uiElement instanceof EntityBackedUIElement)) {
            throw new IllegalStateException("Unknown type of UI Element: \"" + uiElement.getClass().getSimpleName() + "\"");
        }
        val entityBackedUiElement = (EntityBackedUIElement) uiElement;
        entityBackedUiElement.getEntityManager()
                             .getComponentOf(entityBackedUiElement.getEntity(), this.componentClass)
                             .ifPresent(component -> this.componentValueSetter.accept(component, value));
    }

    private Optional<T> getValueFromEntity(
            final Entity entity,
            final EntityManager entityManager
    ) {
        return entityManager.getComponentOf(entity, this.componentClass)
                            .map(this.componentToValueMapper);
    }
}
