package fi.jakojaannos.roguelite.engine.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ui.UIElement;
import fi.jakojaannos.roguelite.engine.ui.UIProperty;
import lombok.RequiredArgsConstructor;
import lombok.val;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EntityBackedUIElement implements UIElement {
    private final Entity entity;
    private final EntityManager entityManager;

    @Nullable private UIElement parent;
    private final List<UIElement> children = new ArrayList<>();

    @Override
    public Optional<UIElement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public Collection<UIElement> getChildren() {
        return this.children;
    }

    @Override
    public <T> Optional<T> getProperty(final UIProperty<T> property) {
        if (!(property instanceof ComponentBackedUIProperty)) {
            throw new IllegalStateException("Unknown property type: " + property.getClass().getSimpleName());
        }

        val componentBackedProperty = (ComponentBackedUIProperty<T, ?>) property;
        return componentBackedProperty.getValueFromEntity(this.entity, this.entityManager);
    }

    public void setParent(final UIElement parent) {
        this.parent = parent;
    }

    public void addChild(final UIElement child) {
        this.children.add(child);
    }
}
