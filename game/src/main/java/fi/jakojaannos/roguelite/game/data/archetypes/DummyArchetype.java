package fi.jakojaannos.roguelite.game.data.archetypes;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import lombok.val;

public class DummyArchetype {


    public static Entity create(
             final EntityManager entityManager,
            double x,
            double y
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
        val dummy = entityManager.createEntity();
        entityManager.addComponentTo(dummy, transform);
        entityManager.addComponentTo(dummy, new Health(10));
        entityManager.addComponentTo(dummy, new Collider(CollisionLayer.OVERLAP_ALL));

        return dummy;
    }

}
