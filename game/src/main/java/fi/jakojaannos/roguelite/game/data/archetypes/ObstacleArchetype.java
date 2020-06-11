package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.ObstacleTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;

public class ObstacleArchetype {
    public static EntityHandle create(
            final Entities entities,
            final Transform transform,
            final double size
    ) {
        return entities.createEntity(new Transform(transform),
                                     new Collider(CollisionLayer.OBSTACLE,
                                                                size,
                                                                size,
                                                                size / 2.0,
                                                                size / 2.0),
                                     new SpriteInfo("sprites/obstacle"),
                                     new ObstacleTag());
    }
}
