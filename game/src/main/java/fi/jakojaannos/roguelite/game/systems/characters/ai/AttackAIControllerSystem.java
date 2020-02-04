package fi.jakojaannos.roguelite.game.systems.characters.ai;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.EnemyTag;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class AttackAIControllerSystem implements ECSSystem {
    private final Random random = new Random(123456);

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(AttackAI.class)
                    .withComponent(WeaponInput.class)
                    .withComponent(AttackAbility.class)
                    .withComponent(Transform.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();

        final var maybeLocalPlayer = world.getOrCreateResource(Players.class).getLocalPlayer();
        entities.forEach(entity -> {
            final var input = entityManager.getComponentOf(entity, WeaponInput.class).orElseThrow();
            final var attackAbility = entityManager.getComponentOf(entity, AttackAbility.class).orElseThrow();
            final var ai = entityManager.getComponentOf(entity, AttackAI.class).orElseThrow();
            final var position = entityManager.getComponentOf(entity, Transform.class)
                                              .orElseThrow().position;

            if (maybeLocalPlayer.isPresent() && entityManager.hasComponent(entity, EnemyTag.class)) {
                ai.setAttackTarget(maybeLocalPlayer.get());
            }

            final var maybeTarget = ai.getAttackTarget();
            if (maybeTarget.isPresent()) {
                final var target = maybeTarget.get();
                final var targetPosition = entityManager.getComponentOf(target, Transform.class)
                                                        .orElseThrow().position;

                if (canAttackTarget(ai, position, targetPosition)) {
                    input.attack = true;
                    // FIXME: Take aim lead with noise
                    attackAbility.targetPosition = targetPosition;
                    return;
                }
            }

            input.attack = false;
        });
    }

    private boolean canAttackTarget(
            final AttackAI ai,
            final Vector2d position,
            final Vector2d targetPosition
    ) {
        final var attackRangeSq = ai.attackRange * ai.attackRange;
        final var distanceSq = targetPosition.distanceSquared(position);
        return distanceSq <= attackRangeSq;
    }
}
