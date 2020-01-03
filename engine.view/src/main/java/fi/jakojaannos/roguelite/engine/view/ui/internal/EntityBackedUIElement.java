package fi.jakojaannos.roguelite.engine.view.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class EntityBackedUIElement implements UIElement {
    @Getter private final Entity entity;
    @Getter private final EntityManager entityManager;

    @Nullable private UIElement parent;
    private final Set<UIElement> children = new HashSet<>();

    @Override
    public Optional<UIElement> getParent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public Collection<UIElement> getChildren() {
        return this.children;
    }

    public void setParent(final UIElement parent) {
        this.parent = parent;
    }

    public void addChild(final UIElement child) {
        this.children.add(child);
    }
}
