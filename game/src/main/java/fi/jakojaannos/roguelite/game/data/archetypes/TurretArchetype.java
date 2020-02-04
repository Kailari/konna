package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.BasicTurretComponent;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;

public class TurretArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final Transform transform
    ) {
        final var turret = entityManager.createEntity();
        entityManager.addComponentTo(turret, transform);
        entityManager.addComponentTo(turret, new BasicTurretComponent());
        entityManager.addComponentTo(turret, new SpriteInfo("sprites/turret"));
        entityManager.addComponentTo(turret, new Collider(CollisionLayer.NONE, 2.0, 2.0, 1.0, 1.0));

        return turret;
    }
}
