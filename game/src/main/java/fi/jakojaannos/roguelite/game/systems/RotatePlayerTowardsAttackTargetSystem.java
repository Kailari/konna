package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;

public class RotatePlayerTowardsAttackTargetSystem implements ECSSystem {
    private static final Vector2dc ROTATION_ZERO_DIRECTION = new Vector2d(0.0, -1.0);

    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .withComponent(PlayerTag.class)
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
            final var abilities = entityManager.getComponentOf(entity, AttackAbility.class)
                                               .orElseThrow();

            abilities.targetPosition.sub(transform.position, this.tmpDirection);
            transform.rotation = -this.tmpDirection.angle(ROTATION_ZERO_DIRECTION);
        });
    }
}
