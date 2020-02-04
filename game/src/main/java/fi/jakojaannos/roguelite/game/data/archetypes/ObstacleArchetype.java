package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.ObstacleTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;

public class ObstacleArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final Transform transform,
            final double size
    ) {
        final var obstacle = entityManager.createEntity();
        entityManager.addComponentTo(obstacle, transform);
        entityManager.addComponentTo(obstacle, new Collider(CollisionLayer.OBSTACLE,
                                                            size,
                                                            size,
                                                            size / 2.0,
                                                            size / 2.0));
        entityManager.addComponentTo(obstacle, new SpriteInfo("sprites/obstacle"));
        entityManager.addComponentTo(obstacle, new ObstacleTag());
        return obstacle;
    }
}
