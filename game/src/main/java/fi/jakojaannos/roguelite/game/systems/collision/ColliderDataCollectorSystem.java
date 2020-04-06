package fi.jakojaannos.roguelite.game.systems.collision;

import java.util.ArrayList;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.legacy.World;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class ColliderDataCollectorSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
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
        final var colliders = world.getOrCreateResource(Colliders.class);

        colliders.solidForLayer.clear();
        colliders.overlapsWithLayer.clear();
        entities.forEach(entity -> {
            final var collider = world.getEntityManager().getComponentOf(entity, Collider.class).orElseThrow();
            final var transform = world.getEntityManager().getComponentOf(entity, Transform.class).orElseThrow();
            final var colliderEntity = new Colliders.ColliderEntity(entity, transform, collider);
            for (final var layer : CollisionLayer.values()) {
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
