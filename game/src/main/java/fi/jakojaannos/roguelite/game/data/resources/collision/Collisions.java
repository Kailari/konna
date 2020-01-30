package fi.jakojaannos.roguelite.game.data.resources.collision;

import java.util.*;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionEvent;

/**
 * Manages {@link CollisionEvent CollisionEvents} for entities.
 */
public class Collisions implements Resource {
    private final Map<Entity, List<CollisionEvent>> collisionEvents = new HashMap<>();

    /**
     * Gets all collision events currently recorded for given entity. This usually means only events fired earlier
     * during the current tick.
     *
     * @param entity entity to fetch collisions for
     *
     * @return collection containing all the collision events
     */
    public Collection<CollisionEvent> getEventsFor(final Entity entity) {
        return collisionEvents.getOrDefault(entity, List.of());
    }

    /**
     * Fires a new collision event for given entity. Does not automatically fire event for "other" entity, that should
     * be done separately, if needed.
     *
     * @param entity entity to fire the event for
     * @param event  event to fire
     */
    public void fireCollisionEvent(final Entity entity, final CollisionEvent event) {
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
