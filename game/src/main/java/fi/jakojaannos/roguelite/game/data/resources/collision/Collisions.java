package fi.jakojaannos.roguelite.game.data.resources.collision;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionEvent;

/**
 * Manages {@link CollisionEvent CollisionEvents} for entities.
 */
public class Collisions {
    private final Map<EntityHandle, List<CollisionEvent>> collisionEvents = new ConcurrentHashMap<>();

    /**
     * Gets all collision events currently recorded for given entity. This usually means only events fired earlier
     * during the current tick.
     *
     * @param entity entity to fetch collisions for
     *
     * @return collection containing all the collision events
     */
    public Collection<CollisionEvent> getEventsFor(final EntityHandle entity) {
        return this.collisionEvents.getOrDefault(entity, List.of());
    }

    /**
     * Fires a new collision event for given entity. Does not automatically fire event for "other" entity, that should
     * be done separately, if needed.
     *
     * @param entity entity to fire the event for
     * @param event  event to fire
     */
    public void fireCollisionEvent(final EntityHandle entity, final CollisionEvent event) {
        this.collisionEvents.computeIfAbsent(entity, key -> new ArrayList<>())
                            .add(event);
    }

    /**
     * Clears all currently recorded collision events for all entities.
     */
    public void clear() {
        this.collisionEvents.clear();
    }
}
