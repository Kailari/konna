package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterAbilities;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import lombok.val;
import org.joml.Vector2d;
import org.joml.Vector2dc;

import java.util.stream.Stream;

public class RotatePlayerTowardsAttackTargetSystem implements ECSSystem {
    private static final Vector2dc ROTATION_ZERO_DIRECTION = new Vector2d(0.0, -1.0);

    private final Vector2d tmpDirection = new Vector2d();

    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .withComponent(PlayerTag.class)
                    .withComponent(Transform.class)
                    .withComponent(CharacterAbilities.class)
                    .tickAfter(PlayerInputSystem.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            val transform = entityManager.getComponentOf(entity, Transform.class)
                                         .orElseThrow();
            val abilities = entityManager.getComponentOf(entity, CharacterAbilities.class)
                                         .orElseThrow();

            abilities.attackTarget.sub(transform.position, tmpDirection);
            transform.rotation = -tmpDirection.angle(ROTATION_ZERO_DIRECTION);
        });
    }
}
