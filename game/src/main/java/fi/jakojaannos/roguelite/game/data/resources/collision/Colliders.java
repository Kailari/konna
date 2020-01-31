package fi.jakojaannos.roguelite.game.data.resources.collision;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyVelocitySystem;
import fi.jakojaannos.roguelite.game.systems.collision.CollisionLayer;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.joml.Rectangled;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class Colliders implements Resource {
    public final Map<CollisionLayer, List<ColliderEntity>> solidForLayer = new HashMap<>();
    public final Map<CollisionLayer, List<ColliderEntity>> overlapsWithLayer = new HashMap<>();

    public void collectRelevantEntities(
            final Entity entity,
            final CollisionLayer layer,
            final Rectangled bounds,
            final Consumer<ApplyVelocitySystem.CollisionCandidate> colliderConsumer,
            final Consumer<ApplyVelocitySystem.CollisionCandidate> overlapConsumer
    ) {
        val potentialCollisions = this.solidForLayer.computeIfAbsent(layer, key -> List.of());
        for (val other : potentialCollisions) {
            if (other.entity.getId() == entity.getId()) {
                continue;
            }
            colliderConsumer.accept(new ApplyVelocitySystem.CollisionCandidate(other));
        }

        val potentialOverlaps = this.overlapsWithLayer.computeIfAbsent(layer, key -> List.of());
        for (val other : potentialOverlaps) {
            if (other.entity.getId() == entity.getId()) {
                continue;
            }
            overlapConsumer.accept(new ApplyVelocitySystem.CollisionCandidate(other));
        }
    }

    @RequiredArgsConstructor
    public static final class ColliderEntity {
        public final Entity entity;
        public final Transform transform;
        public final Collider collider;
    }
}
