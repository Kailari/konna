package fi.jakojaannos.roguelite.engine.view.ui.internal;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class EntityBackedUIElement implements UIElement {
    @Getter private final Entity entity;
    @Getter private final EntityManager entityManager;

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

    public void setParent(final UIElement parent) {
        this.parent = parent;
    }

    public void addChild(final UIElement child) {
        this.children.add(child);
    }
}
