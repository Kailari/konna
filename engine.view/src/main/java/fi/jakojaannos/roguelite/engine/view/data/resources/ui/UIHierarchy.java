package fi.jakojaannos.roguelite.engine.view.data.resources.ui;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.internal.EntityBackedUIElement;

public class UIHierarchy {
    private final Map<EntityHandle, List<EntityHandle>> children = new ConcurrentHashMap<>();
    private final Map<EntityHandle, EntityHandle> parents = new ConcurrentHashMap<>();

    private final Map<EntityHandle, EntityBackedUIElement> elements = new HashMap<>();
    private final List<UIElement> roots = new ArrayList<>();

    public Stream<UIElement> getRoots() {
        return this.roots.stream();
    }

    public Stream<UIElement> getElements() {
        return this.elements.values()
                            .stream()
                            .map(UIElement.class::cast);
    }

    public void clear() {
        this.children.clear();
        this.parents.clear();
        this.roots.clear();
    }

    public void update(
            final EntityHandle child,
            @Nullable final EntityHandle parent
    ) {
        final var childElement = getOrCreateElementFor(child);
        if (parent == null) {
            this.roots.add(childElement);
        } else {
            this.parents.put(child, parent);
            this.children.computeIfAbsent(parent, key -> new ArrayList<>())
                         .add(child);

            final var parentElement = getOrCreateElementFor(parent);
            childElement.setParent(parentElement);
            parentElement.addChild(childElement);
        }
    }

    public EntityBackedUIElement getOrCreateElementFor(final EntityHandle entity) {
        return this.elements.computeIfAbsent(entity, EntityBackedUIElement::new);
    }

    public Optional<EntityHandle> getParentOf(final EntityHandle entity) {
        return Optional.ofNullable(this.parents.get(entity));
    }

    public boolean isParentOf(final EntityHandle a, final EntityHandle b) {
        return this.children.containsKey(a) && this.children.get(a).contains(b);
    }

    public int parentsFirst(final EntityHandle a, final EntityHandle b) {
        if (isParentOf(a, b)) {
            return -1;
        } else if (isParentOf(b, a)) {
            return 1;
        } else {
            return 0;
        }
    }
}
