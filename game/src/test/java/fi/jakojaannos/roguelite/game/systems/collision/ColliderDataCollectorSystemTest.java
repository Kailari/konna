package fi.jakojaannos.roguelite.game.systems.collision;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.legacy.LegacyWorld;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ColliderDataCollectorSystemTest {
    private LegacyWorld world;
    private Entity entityA;
    private Entity entityB;
    private ColliderDataCollectorSystem system;

    @BeforeEach
    void beforeEach() {
        world = World.createNew();
        final EntityManager entityManager = world.getEntityManager();

        entityA = entityManager.createEntity();
        entityManager.addComponentTo(entityA, new Transform());
        entityManager.addComponentTo(entityA, new Collider(CollisionLayer.ENEMY));

        entityB = entityManager.createEntity();
        entityManager.addComponentTo(entityB, new Transform());
        entityManager.addComponentTo(entityB, new Collider(CollisionLayer.OBSTACLE));

        system = new ColliderDataCollectorSystem();
        for (CollisionLayer layer : CollisionLayer.values()) {
            Entity other = entityManager.createEntity();
            entityManager.addComponentTo(other, new Transform());
            entityManager.addComponentTo(other, new Collider(layer));
        }

        entityManager.applyModifications();
    }

    @Test
    void entityWithColliderIsAddedToRelevantLists() {
        system.tick(Stream.of(entityA, entityB), world);

        Colliders colliders = world.getOrCreateResource(Colliders.class);
        assertTrue(colliders.overlapsWithLayer.get(CollisionLayer.PLAYER_PROJECTILE)
                                              .stream()
                                              .anyMatch(e -> e.entity().getId() == entityA.getId()));
        assertTrue(colliders.solidForLayer.get(CollisionLayer.PLAYER)
                                          .stream()
                                          .anyMatch(e -> e.entity().getId() == entityB.getId()));
    }
}
