package fi.jakojaannos.roguelite.game.systems.collision;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import lombok.val;

import java.util.ArrayList;
import java.util.stream.Stream;

public class ColliderDataCollectorSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.EARLY_TICK)
                    .withComponent(Collider.class)
                    .withComponent(Transform.class)
                    .requireResource(Colliders.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val colliders = world.getOrCreateResource(Colliders.class);

        colliders.solidForLayer.clear();
        colliders.overlapsWithLayer.clear();
        entities.forEach(entity -> {
            val collider = world.getEntityManager().getComponentOf(entity, Collider.class).orElseThrow();
            val transform = world.getEntityManager().getComponentOf(entity, Transform.class).orElseThrow();
            val colliderEntity = new Colliders.ColliderEntity(entity, transform, collider);
            for (val layer : CollisionLayer.values()) {
                if (collider.layer.isSolidTo(layer)) {
                    colliders.solidForLayer.computeIfAbsent(layer, key -> new ArrayList<>())
                                           .add(colliderEntity);
                } else if (collider.layer.canOverlapWith(layer)) {
                    colliders.overlapsWithLayer.computeIfAbsent(layer, key -> new ArrayList<>())
                                               .add(colliderEntity);
                }
            }
        });
    }
}
