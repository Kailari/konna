package fi.jakojaannos.roguelite.game.systems.collision;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.CollisionLayer;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.resources.collision.Colliders;

public class ColliderDataCollectorSystem implements EcsSystem<ColliderDataCollectorSystem.Resources, ColliderDataCollectorSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var colliders = resources.colliders;

        colliders.solidForLayer.clear();
        colliders.overlapsWithLayer.clear();
        entities.forEach(entity -> {
            final var collider = entity.getData().collider;
            final var transform = entity.getData().transform;
            final var colliderEntity = new Colliders.ColliderEntity(entity.getHandle(), transform, collider);
            for (final var layer : CollisionLayer.values()) {
                if (collider.layer.isSolidTo(layer)) {
                    colliders.solidForLayer.computeIfAbsent(layer, key -> new ConcurrentLinkedQueue<>())
                                           .add(colliderEntity);
                } else if (collider.layer.canOverlapWith(layer)) {
                    colliders.overlapsWithLayer.computeIfAbsent(layer, key -> new ConcurrentLinkedQueue<>())
                                               .add(colliderEntity);
                }
            }
        });
    }

    public static record EntityData(Collider collider, Transform transform) {}

    public static record Resources(Colliders colliders) {}
}
