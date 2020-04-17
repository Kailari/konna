package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.tilemap.TileMap;
import fi.jakojaannos.roguelite.engine.tilemap.TileType;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.TileMapLayer;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.data.resources.collision.Collisions;
import fi.jakojaannos.roguelite.game.systems.collision.ColliderDataCollectorSystem;
import fi.jakojaannos.roguelite.game.systems.collision.TileColliderCollectorSystem;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.junitextension.Assertions.assertEqualsExt;
import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.*;

class ApplyVelocitySystemTest {
    private Velocity velocity;
    private Transform transform;

    void stateWithCollider(final World world) {
        world.registerResource(new Collisions());
        world.registerResource(new Colliders());

        velocity = new Velocity();
        transform = new Transform(0.0, 0.0);
        world.createEntity(velocity,
                           transform,
                           new Collider(CollisionLayer.COLLIDE_ALL, 1.0));
    }

    void stateWithoutCollider(final World world) {
        world.registerResource(new Collisions());
        world.registerResource(new Colliders());

        velocity = new Velocity();
        transform = new Transform(0.0, 0.0);
        world.createEntity(velocity,
                           transform);
    }

    @Test
    void entityWithColliderDoesNotMoveWhenVelocityIsZero() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> velocity.set(0.0))
                  .runsSingleTick()
                  .expect(state -> assertEqualsExt(new Vector2d(0.0, 0.0), transform.position, 0.0));
    }

    @Test
    void entityWithColliderMovesWhenVelocityIsNonZero() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> velocity.set(10.0))
                  .runsForTicks(50)
                  .expect(state -> assertEqualsExt(new Vector2d(10.0, 10.0), transform.position, 0.02));
    }

    @Test
    void entityWithoutColliderDoesNotMoveWhenVelocityIsZero() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithoutCollider)
                  .withState(world -> velocity.set(0.0))
                  .runsSingleTick()
                  .expect(state -> assertEqualsExt(new Vector2d(0.0, 0.0), transform.position, 0.0));
    }

    @Test
    void entityWithoutColliderMovesWhenVelocityIsNonZero() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithoutCollider)
                  .withState(world -> velocity.set(10.0))
                  .runsForTicks(50)
                  .expect(state -> assertEqualsExt(new Vector2d(10.0, 10.0), transform.position, 0.02));
    }

    @Test
    void entityWithNonSolidColliderDoesNotBlockMovement() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> {
                      world.createEntity(new Transform(1.0, 0.0),
                                         new Collider(CollisionLayer.NONE));
                      velocity.set(10.0, 0.75);
                  })
                  .runsForTicks(50)
                  .expect(state -> assertEqualsExt(new Vector2d(10.0, 0.75), transform.position, 0.05));
    }

    @Test
    void entityWithSolidColliderBlocksMovement() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> {
                      world.createEntity(new Transform(1.0, 0.0),
                                         new Collider(CollisionLayer.COLLIDE_ALL));
                      velocity.set(1.0, 0.0);
                  })
                  .runsForTicks(50)
                  .expect(state -> assertEquals(0.0, transform.position.x, 0.05));
    }

    @Test
    void entitySlidesHorizontallyWhenCollidingAgainstSolidEntityFromBelow() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> {
                      world.createEntity(new Transform(0.0, 1.0),
                                         new Collider(CollisionLayer.COLLIDE_ALL));
                      velocity.set(0.25, 1.0);
                  })
                  .runsForTicks(50)
                  .expect(state -> assertAll(() -> assertNotEquals(0.0, transform.position.x, 0.05),
                                             () -> assertEquals(0.0, transform.position.y, 0.05)));
    }

    @Test
    void entitySlidesVerticallyWhenCollidingAgainstSolidEntityFromSide() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> {
                      world.createEntity(new Transform(1.0, 0.0),
                                         new Collider(CollisionLayer.COLLIDE_ALL));
                      velocity.set(1.0, 0.25);
                  })
                  .runsForTicks(50)
                  .expect(state -> assertAll(() -> assertEquals(0.0, transform.position.x, 0.05),
                                             () -> assertNotEquals(0.0, transform.position.y, 0.05)));
    }

    @Test
    void tileLayerDoesNotBlockMovementIfCollisionIsDisabledForLayer() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> {
                      final var empty = new TileType(0, false);
                      final var block = new TileType(1, true);
                      final var tileMap = new TileMap<>(empty);
                      tileMap.setTile(1, 0, block);

                      world.createEntity(new TileMapLayer(tileMap, false));

                      velocity.set(1.0, 0.1);
                      transform.position.x = 0;
                  })
                  .runsForTicks(50)
                  .expect(state -> assertEqualsExt(new Vector2d(1.0, 0.1), transform.position, 0.05));
    }

    @Test
    void entitySlidesVerticallyWhenCollidingAgainstTileFromSide() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> {
                      final var empty = new TileType(0, false);
                      final var block = new TileType(1, true);
                      final var tileMap = new TileMap<>(empty);
                      tileMap.setTile(1, 0, block);

                      world.createEntity(new TileMapLayer(tileMap, true));

                      velocity.set(1.0, 0.1);
                      transform.position.x = 0;
                  })
                  .runsForTicks(50)
                  .expect(state -> assertAll(() -> assertEquals(0.0, transform.position.x, 0.05),
                                             () -> assertNotEquals(0.0, transform.position.y, 0.05)));
    }

    @Test
    void nonSolidTilesDoNotBlockMovement() {
        whenGame().withSystems(new ColliderDataCollectorSystem(),
                               new TileColliderCollectorSystem(),
                               new ApplyVelocitySystem())
                  .withState(this::stateWithCollider)
                  .withState(world -> {
                      final var empty = new TileType(0, false);
                      final var nonSolid = new TileType(1, false);
                      final var tileMap = new TileMap<>(empty);
                      tileMap.setTile(1, 0, nonSolid);

                      world.createEntity(new TileMapLayer(tileMap, true));

                      velocity.set(1.0, 0.1);
                      transform.position.x = 0;
                  })
                  .runsForTicks(50)
                  .expect(state -> assertEqualsExt(new Vector2d(1.0, 0.1), transform.position, 0.05));
    }
}
