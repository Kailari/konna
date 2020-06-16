package fi.jakojaannos.roguelite.game.systems.collision;

import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EntityHandle;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColliderDataCollectorSystemTest {
    private EntityHandle enemyEntity;
    private EntityHandle obstacleEntity;
    private Colliders colliders;

    void beforeEach(final World world) {
        colliders = new Colliders();
        world.registerResource(colliders);

        enemyEntity = world.createEntity(new Transform(),
                                         new Collider(CollisionLayer.ENEMY));

        obstacleEntity = world.createEntity(new Transform(),
                                            new Collider(CollisionLayer.OBSTACLE));

        for (CollisionLayer layer : CollisionLayer.values()) {
            world.createEntity(new Transform(),
                               new Collider(layer));
        }
    }

    @Test
    void entityWithColliderIsAddedToRelevantLists() {
        whenGame().withSystems(new ColliderDataCollectorSystem())
                  .withState(this::beforeEach)
                  .runsSingleTick()
                  .expect(state -> {
                      assertTrue(colliders.overlapsWithLayer.get(CollisionLayer.PLAYER_PROJECTILE)
                                                            .stream()
                                                            .anyMatch(e -> e.entity().getId() == enemyEntity.getId()));
                      assertTrue(colliders.solidForLayer.get(CollisionLayer.PLAYER)
                                                        .stream()
                                                        .anyMatch(e -> e.entity().getId() == obstacleEntity.getId()));
                  });
    }
}
