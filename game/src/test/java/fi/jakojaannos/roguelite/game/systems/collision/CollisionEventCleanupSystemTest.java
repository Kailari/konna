package fi.jakojaannos.roguelite.game.systems.collision;

import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.expectEntity;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollisionEventCleanupSystemTest {
    private Collisions collisions;
    private EntityHandle entity;

    void beforeEach(final World world) {
        collisions = new Collisions();
        world.registerResource(collisions);

        entity = world.createEntity(new Collider(CollisionLayer.COLLIDE_ALL),
                                    new RecentCollisionTag());

        final var other = world.createEntity();
        collisions.fireCollisionEvent(entity,
                                      new CollisionEvent(Collision.entity(Collision.Mode.COLLISION,
                                                                          other)));
        collisions.fireCollisionEvent(entity,
                                      new CollisionEvent(Collision.entity(Collision.Mode.COLLISION,
                                                                          other)));
        collisions.fireCollisionEvent(entity,
                                      new CollisionEvent(Collision.entity(Collision.Mode.COLLISION,
                                                                          other)));
    }

    @Test
    void collisionEventsAreCleanedUp() {
        whenGame().withSystems(new CollisionEventCleanupSystem())
                  .withState(this::beforeEach)
                  .runsSingleTick()
                  .expect(state -> assertTrue(collisions.getEventsFor(entity).isEmpty()));
    }

    @Test
    void recentCollisionTagIsRemoved() {
        whenGame().withSystems(new CollisionEventCleanupSystem())
                  .withState(this::beforeEach)
                  .runsSingleTick()
                  .expect(state -> expectEntity(entity).toNotHaveComponent(RecentCollisionTag.class));
    }
}
