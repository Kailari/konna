package fi.jakojaannos.roguelite.engine.view.ui.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.*;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;

@RequiredArgsConstructor
public class EntityBackedUIElement implements UIElement {
    @Getter private final Entity entity;
    @Getter private final EntityManager entityManager;
    private final Set<UIElement> children = new HashSet<>();
    @Nullable private UIElement parent;

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

    public void addChild(final UIElement child) {
        this.children.add(child);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final EntityBackedUIElement that = (EntityBackedUIElement) o;
        return entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entity);
    }
}
