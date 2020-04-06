package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.legacy.World;
import fi.jakojaannos.roguelite.game.data.components.RotateTowardsVelocityTag;
import fi.jakojaannos.roguelite.game.data.components.Velocity;

public class RotateTowardsVelocitySystem implements ECSSystem {
    private static final Vector2dc ROTATION_ZERO_DIRECTION = new Vector2d(0.0, -1.0);

    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .withComponent(RotateTowardsVelocityTag.class)
                    .withComponent(Transform.class)
                    .withComponent(Velocity.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            final var transform = entityManager.getComponentOf(entity, Transform.class)
                                               .orElseThrow();
            final var velocity = entityManager.getComponentOf(entity, Velocity.class)
                                              .orElseThrow();

            final Vector2d direction;
            if (velocity.lengthSquared() == 0) {
                direction = new Vector2d(0.0, -1.0);
            } else {
                direction = velocity.normalize(this.tmpDirection);
            }

            transform.rotation = -direction.angle(ROTATION_ZERO_DIRECTION);
        });
    }
}
