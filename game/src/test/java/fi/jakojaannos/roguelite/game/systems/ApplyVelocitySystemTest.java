package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.game.systems.collision.ColliderDataCollectorSystem;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplyVelocitySystemTest {
    private ColliderDataCollectorSystem dataCollectorSystem;
    private ApplyVelocitySystem system;
    private World world;
    private EntityManager entityManager;
    private Entity entity;
    private Velocity velocity;
    private Transform transform;
    private Collider collider;

    @BeforeEach
    void beforeEach() {
        entityManager = EntityManager.createNew(256, 32);
        world = World.createNew(entityManager);

        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.createOrReplaceResource(Time.class, time);

        entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, velocity = new Velocity());
        entityManager.addComponentTo(entity, transform = new Transform(0.0, 0.0));
        entityManager.addComponentTo(entity, collider = new Collider(CollisionLayer.COLLIDE_ALL));
        collider.height = collider.width = 1;

        system = new ApplyVelocitySystem();
        dataCollectorSystem = new ColliderDataCollectorSystem();
    }

    @Test
    void entityWithColliderDoesNotMoveWhenVelocityIsZero() {
        velocity.velocity = new Vector2d(0.0);

        world.getEntityManager().applyModifications();
        dataCollectorSystem.tick(Stream.of(entity), world);
        system.tick(Stream.of(entity), world);

        assertEquals(0.0, transform.position.x);
        assertEquals(0.0, transform.position.y);
    }

    @Test
    void entityWithColliderMovesWhenVelocityIsNonZero() {
        velocity.velocity = new Vector2d(10.0);

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity), world);
            system.tick(Stream.of(entity), world);
        }

        assertEquals(10.0, transform.position.x, 0.02);
        assertEquals(10.0, transform.position.y, 0.02);
    }

    @Test
    void entityWithoutColliderDoesNotMoveWhenVelocityIsZero() {
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);

        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.createOrReplaceResource(Time.class, time);

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, velocity = new Velocity());
        entityManager.addComponentTo(entity, transform = new Transform(0.0, 0.0));
        velocity.velocity = new Vector2d(0.0);

        world.getEntityManager().applyModifications();
        system.tick(Stream.of(entity), world);

        assertEquals(0.0, transform.position.x);
        assertEquals(0.0, transform.position.y);
    }

    @Test
    void entityWithoutColliderMovesWhenVelocityIsNonZero() {
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);

        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.createOrReplaceResource(Time.class, time);

        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, velocity = new Velocity());
        entityManager.addComponentTo(entity, transform = new Transform(0.0, 0.0));
        velocity.velocity = new Vector2d(10.0);

        for (int i = 0; i < 50; ++i) {
            world.getEntityManager().applyModifications();
            system.tick(Stream.of(entity), world);
        }

        assertEquals(10.0, transform.position.x, 0.01);
        assertEquals(10.0, transform.position.y, 0.01);
    }

    @Test
    void entityWithNonSolidColliderDoesNotBlockMovement() {
        Entity other = entityManager.createEntity();
        Transform otherTransform = new Transform(1.0, 0.0);
        Collider otherCollider = new Collider(CollisionLayer.NONE);
        entityManager.addComponentTo(other, otherCollider);
        entityManager.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(10.0, 0.75);

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity, other), world);
            system.tick(Stream.of(entity), world);
        }

        assertEquals(10.0, transform.position.x, 0.01);
        assertEquals(0.75, transform.position.y, 0.01);
    }

    @Test
    void entityWithSolidColliderBlocksMovement() {
        Entity other = entityManager.createEntity();
        Transform otherTransform = new Transform(1.0, 0.0);
        Collider otherCollider = new Collider(CollisionLayer.COLLIDE_ALL);
        entityManager.addComponentTo(other, otherCollider);
        entityManager.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(1.0, 0.1);

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity, other), world);
            system.tick(Stream.of(entity), world);
        }

        assertEquals(0.0, transform.position.x);
    }

    @Test
    void entitySlidesHorizontallyWhenCollidingAgainstSolidEntityFromBelow() {
        Entity other = entityManager.createEntity();
        Transform otherTransform = new Transform(0.0, 1.0);
        Collider otherCollider = new Collider(CollisionLayer.COLLIDE_ALL);
        entityManager.addComponentTo(other, otherCollider);
        entityManager.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(0.1, 1.0);

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity, other), world);
            system.tick(Stream.of(entity), world);
        }

        assertNotEquals(0.0, transform.position.x);
        assertEquals(0.0, transform.position.y);
    }

    @Test
    void entitySlidesVerticallyWhenCollidingAgainstSolidEntityFromSide() {
        Entity other = entityManager.createEntity();
        Transform otherTransform = new Transform(1.0, 0.0);
        Collider otherCollider = new Collider(CollisionLayer.COLLIDE_ALL);
        entityManager.addComponentTo(other, otherCollider);
        entityManager.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(1.0, 0.25);
        transform.position.x = -0.05;

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity, other), world);
            system.tick(Stream.of(entity), world);
        }

        assertEquals(0.0, transform.position.x, 0.01);
        assertNotEquals(0.0, transform.position.y, 0.01);
    }

    @Test
    void tileLayerDoesNotBlockMovementIfCollisionIsDisabledForLayer() {
        Entity other = entityManager.createEntity();
        TileType empty = new TileType(0, false);
        TileType block = new TileType(1, true);
        TileMap<TileType> tileMap = new TileMap<>(empty);
        tileMap.setTile(1, 0, block);
        TileMapLayer layer = new TileMapLayer(tileMap);
        layer.collisionEnabled = false;
        entityManager.addComponentTo(other, layer);

        velocity.velocity = new Vector2d(1.0, 0.1);
        transform.position.x = 0;

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity), world);
            system.tick(Stream.of(entity), world);
        }

        assertEquals(1.0, transform.position.x, 0.01);
        assertEquals(0.1, transform.position.y, 0.01);
    }

    @Test
    void entitySlidesVerticallyWhenCollidingAgainstTileFromSide() {
        Entity other = entityManager.createEntity();
        TileType empty = new TileType(0, false);
        TileType block = new TileType(1, true);
        TileMap<TileType> tileMap = new TileMap<>(empty);
        tileMap.setTile(1, 0, block);
        entityManager.addComponentTo(other, new TileMapLayer(tileMap));

        velocity.velocity = new Vector2d(1.0, 0.1);

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity), world);
            system.tick(Stream.of(entity), world);
        }

        assertEquals(0.0, transform.position.x);
        assertNotEquals(0.0, transform.position.y);
    }

    @Test
    void nonSolidTilesDoNotBlockMovement() {
        Entity other = entityManager.createEntity();
        TileType empty = new TileType(0, false);
        TileType nonSolid = new TileType(1, false);
        TileMap<TileType> tileMap = new TileMap<>(empty);
        tileMap.setTile(1, 0, nonSolid);
        entityManager.addComponentTo(other, new TileMapLayer(tileMap));

        velocity.velocity = new Vector2d(1.0, 0.1);

        world.getEntityManager().applyModifications();
        for (int i = 0; i < 50; ++i) {
            dataCollectorSystem.tick(Stream.of(entity), world);
            system.tick(Stream.of(entity), world);
        }

        assertEquals(1.0, transform.position.x, 0.01);
        assertEquals(0.1, transform.position.y, 0.01);
    }

    @Test
    void collidingWithDiagonalSurfaceDoesNotBlowUp() {
        Entity other = entityManager.createEntity();
        Transform otherTransform = new Transform(1.0, 0.5);
        otherTransform.rotation = 45.0;
        Collider otherCollider = new Collider(CollisionLayer.COLLIDE_ALL);
        entityManager.addComponentTo(other, otherCollider);
        entityManager.addComponentTo(other, otherTransform);

        velocity.velocity = new Vector2d(1.0, 0);
        transform.position.x = -1.5;
        transform.position.y = -1;

        world.getEntityManager().applyModifications();
        assertTimeout(Duration.ofMillis(500), () -> {
            for (int i = 0; i < 150; ++i) {
                dataCollectorSystem.tick(Stream.of(entity, other), world);
                system.tick(Stream.of(entity), world);
            }
        });
    }
}
