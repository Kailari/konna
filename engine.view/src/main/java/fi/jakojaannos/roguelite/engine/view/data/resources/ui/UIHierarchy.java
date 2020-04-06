package fi.jakojaannos.roguelite.engine.view.data.resources.ui;

import java.util.*;
import java.util.stream.Stream;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ProvidedResource;
import fi.jakojaannos.roguelite.engine.view.ui.UIElement;
import fi.jakojaannos.roguelite.engine.view.ui.internal.EntityBackedUIElement;

public class UIHierarchy implements ProvidedResource {
    private final Map<Entity, List<Entity>> children = new HashMap<>();
    private final Map<Entity, Entity> parents = new HashMap<>();

    private final Map<Entity, EntityBackedUIElement> elements = new HashMap<>();
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
            final EntityManager entityManager,
            final Entity child,
            @Nullable final Entity parent
    ) {
        final var childElement = getOrCreateElementFor(child, entityManager);
        if (parent == null) {
            this.roots.add(childElement);
        } else {
            this.parents.put(child, parent);
            this.children.computeIfAbsent(parent, key -> new ArrayList<>())
                         .add(child);

            final var parentElement = getOrCreateElementFor(parent, entityManager);
            childElement.setParent(parentElement);
            parentElement.addChild(childElement);
        }
    }

    public EntityBackedUIElement getOrCreateElementFor(
            final Entity entity,
            final EntityManager entityManager
    ) {
        return this.elements.computeIfAbsent(entity, key -> new EntityBackedUIElement(key, entityManager));
    }

    public Optional<Entity> getParentOf(final Entity entity) {
        return Optional.ofNullable(this.parents.get(entity));
    }

    public boolean isParentOf(final Entity a, final Entity b) {
        return this.children.containsKey(a) && this.children.get(a).contains(b);
    }

    public int parentsFirst(final Entity a, final Entity b) {
        if (isParentOf(a, b)) {
            return -1;
        } else if (isParentOf(b, a)) {
            return 1;
        } else {
            return 0;
        }
    }
}
