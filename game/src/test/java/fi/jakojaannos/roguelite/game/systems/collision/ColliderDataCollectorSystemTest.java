package fi.jakojaannos.roguelite.game.systems.collision;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ColliderDataCollectorSystemTest {
    private EntityManager entityManager;
    private World world;
    private Entity entityA;
    private Entity entityB;
    private ColliderDataCollectorSystem system;
    private List<Entity> entities;


    @BeforeEach
    void beforeEach() {
        entityManager = EntityManager.createNew(256, 32);
        world = World.createNew(entityManager);

        entities = new ArrayList<>();
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
            entities.add(other);
        }

        entities.add(entityA);
        entities.add(entityB);
        entityManager.applyModifications();
    }

    @Test
    void entityWithColliderIsAddedToRelevantLists() {
        system.tick(Stream.of(entityA, entityB), world);

        Colliders colliders = world.getOrCreateResource(Colliders.class);
        assertTrue(colliders.overlapsWithLayer.get(CollisionLayer.PLAYER_PROJECTILE)
                                              .stream()
                                              .anyMatch(e -> e.entity.getId() == entityA.getId()));
        assertTrue(colliders.solidForLayer.get(CollisionLayer.PLAYER)
                                          .stream()
                                          .anyMatch(e -> e.entity.getId() == entityB.getId()));
    }
}
