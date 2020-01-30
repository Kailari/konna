package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.ObstacleTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;

public class ObstacleArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final Transform transform,
            final double size
    ) {
        final var obstacle = entityManager.createEntity();
        entityManager.addComponentTo(obstacle, transform);
        entityManager.addComponentTo(obstacle, createCollider(size));
        entityManager.addComponentTo(obstacle, createSpriteInfo());
        entityManager.addComponentTo(obstacle, new ObstacleTag());
        return obstacle;
    }

    private static Collider createCollider(final double size) {
        final var collider = new Collider(CollisionLayer.OBSTACLE);
        collider.width = size;
        collider.height = size;
        collider.origin.set(size).mul(0.5);
        return collider;
    }

    private static SpriteInfo createSpriteInfo() {
        final var sprite = new SpriteInfo();
        sprite.spriteName = "sprites/obstacle";

        return sprite;
    }
}
