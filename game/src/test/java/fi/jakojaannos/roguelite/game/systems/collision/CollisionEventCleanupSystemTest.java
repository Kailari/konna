package fi.jakojaannos.roguelite.game.systems.collision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.RecentCollisionTag;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CollisionEventCleanupSystemTest {
    private CollisionEventCleanupSystem system;
    private Collisions collisions;
    private World world;
    private EntityManager entityManager;
    private Entity entity;

    @BeforeEach
    void beforeEach() {
        system = new CollisionEventCleanupSystem();
        world = mock(World.class);
        entity = mock(Entity.class);

        Collider collider = new Collider(CollisionLayer.COLLIDE_ALL);
        collisions = new Collisions();

        entityManager = mock(EntityManager.class);
        when(world.getEntityManager()).thenReturn(entityManager);
        when(world.getOrCreateResource(eq(Collisions.class))).thenReturn(collisions);
        when(entityManager.getComponentOf(eq(entity), eq(Collider.class))).thenReturn(Optional.of(collider));
    }

    @Test
    void collisionEventsAreCleanedUp() {
        Entity other = mock(Entity.class);
        collisions.fireCollisionEvent(entity, new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));
        collisions.fireCollisionEvent(entity, new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));
        collisions.fireCollisionEvent(entity, new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));

        system.tick(Stream.of(entity), world);
        assertTrue(collisions.getEventsFor(entity).isEmpty());
    }

    @Test
    void recentCollisionTagIsRemoved() {
        Entity other = mock(Entity.class);
        collisions.fireCollisionEvent(entity, new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));
        collisions.fireCollisionEvent(entity, new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));
        collisions.fireCollisionEvent(entity, new CollisionEvent(Collision.entity(Collision.Mode.COLLISION, other)));

        system.tick(Stream.of(entity), world);
        verify(entityManager).removeComponentFrom(eq(entity), eq(RecentCollisionTag.class));
    }
}
