package fi.jakojaannos.roguelite.engine.view.data.resources.internal;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.Resource;

import java.util.*;

public class UIHierarchy implements Resource {
    private Map<Entity, List<Entity>> children = new HashMap<>();
    private Map<Entity, Entity> parents = new HashMap<>();

    public void clear() {
        this.children.clear();
        this.parents.clear();
    }

    public void setParent(final Entity child, final Entity parent) {
        this.parents.put(child, parent);
        this.children.computeIfAbsent(parent, key -> new ArrayList<>())
                     .add(child);
    }

    public Optional<Entity> getParentOf(final Entity entity) {
        return Optional.ofNullable(this.parents.get(entity));
    }

    public boolean isParentOf(final Entity a, final Entity b) {
        return false;
    }

    public int parentsFirst(final Entity a, final Entity b) {
        if (isParentOf(a, b)) {
            return 1;
        } else if (isParentOf(b, a)) {
            return -1;
        } else {
            return 0;
        }
    }
}
