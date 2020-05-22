package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.RotateTowardsVelocityTag;
import fi.jakojaannos.roguelite.game.data.components.Velocity;

public class RotateTowardsVelocitySystem implements EcsSystem<EcsSystem.NoResources, RotateTowardsVelocitySystem.EntityData, EcsSystem.NoEvents> {
    private static final Vector2d ROTATION_ZERO_DIRECTION = new Vector2d(0.0, 1.0);

    @Override
    public void tick(
            final NoResources noResources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            final var velocity = entity.getData().velocity;

            final Vector2d direction;
            if (velocity.lengthSquared() == 0) {
                direction = ROTATION_ZERO_DIRECTION;
            } else {
                direction = velocity.normalize(new Vector2d());
            }

            transform.rotation = -direction.angle(ROTATION_ZERO_DIRECTION);
        });
    }

    public static record EntityData(
            RotateTowardsVelocityTag rotateTag,
            Transform transform,
            Velocity velocity
    ) {}
}
