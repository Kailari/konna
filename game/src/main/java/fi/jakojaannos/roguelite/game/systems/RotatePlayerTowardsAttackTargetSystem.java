package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.LookAtTargetTag;

public class RotatePlayerTowardsAttackTargetSystem implements ECSSystem {
    private static final Vector2dc ROTATION_ZERO_DIRECTION = new Vector2d(0.0, -1.0);

    private final Vector2d tmpPosition = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .withComponent(LookAtTargetTag.class)
                    .withComponent(Transform.class)
                    .withComponent(AttackAbility.class)
                    .tickAfter(PlayerInputSystem.class);
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
            final var attackAbility = entityManager.getComponentOf(entity, AttackAbility.class)
                                                   .orElseThrow();

            final Vector2d position = transform.position.add(attackAbility.weaponOffset, this.tmpPosition);
            final var targetPosition = attackAbility.targetPosition;

            final Vector2d direction;
            if (position.lengthSquared() == 0 && targetPosition.lengthSquared() == 0) {
                direction = new Vector2d(0.0, -1.0);
            } else {
                direction = targetPosition.sub(position, this.tmpPosition)
                                          .normalize();
            }

            transform.rotation = -direction.angle(ROTATION_ZERO_DIRECTION);
        });
    }
}
