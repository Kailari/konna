package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.joml.Vector2dc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.RotateTowardsVelocityTag;
import fi.jakojaannos.roguelite.game.data.components.Velocity;

public class RotateTowardsVelocitySystem implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(RotateTowardsVelocitySystem.class);

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
        LOG.debug("tick rotate");
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
