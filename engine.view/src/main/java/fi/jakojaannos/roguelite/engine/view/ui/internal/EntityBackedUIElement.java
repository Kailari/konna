package fi.jakojaannos.roguelite.engine.view.ui.internal;

import java.util.*;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;

public class EntityBackedUIElement implements UIElement {
    private final EntityHandle entity;
    private final Set<UIElement> children = new HashSet<>();
    @Nullable private UIElement parent;

    public EntityHandle getEntity() {
        return this.entity;
    }

    @Override
    public Optional<UIElement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    public void setParent(final UIElement parent) {
        this.parent = parent;
    }

    @Override
    public Collection<UIElement> getChildren() {
        return this.children;
    }

    public EntityBackedUIElement(final EntityHandle entity) {
        this.entity = entity;
    }

    public void addChild(final UIElement child) {
        this.children.add(child);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final EntityBackedUIElement that = (EntityBackedUIElement) o;
        return this.entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.entity);
    }
}
