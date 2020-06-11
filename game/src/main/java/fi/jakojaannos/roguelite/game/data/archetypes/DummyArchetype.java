package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.character.Health;

public class DummyArchetype {
    public static Entity create(
            final EntityManager entityManager,
            final double x,
            final double y
    ) {
        return create(
                entityManager,
                new Transform(x, y)
        );
    }

    public static Entity create(
            final EntityManager entityManager,
            final Transform transform
    ) {
        final var dummy = entityManager.createEntity();
        entityManager.addComponentTo(dummy, transform);
        entityManager.addComponentTo(dummy, new Health(10));
        entityManager.addComponentTo(dummy, new Collider(CollisionLayer.OVERLAP_ALL));

        return dummy;
    }

}
