package fi.jakojaannos.roguelite.game.systems.characters.ai;

import org.joml.Vector2d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.PlayerTag;
import fi.jakojaannos.roguelite.game.data.components.character.WeaponInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class AttackAIControllerSystem implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(AttackAIControllerSystem.class);

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
            final var ai = entityManager.getComponentOf(entity, AttackAI.class).orElseThrow();
            final var position = entityManager.getComponentOf(entity, Transform.class)
                                              .orElseThrow().position;

            if (ai.targetTagClass.equals(PlayerTag.class)) {
                maybeLocalPlayer.filter(player -> isTargetValid(entityManager, player, position, ai))
                                .ifPresentOrElse(ai::setAttackTarget, ai::clearAttackTarget);
            } else {
                findOrUpdateTarget(entityManager, ai, position);
            }

            updateAttackInput(entityManager, entity, ai, position);
        });
    }

    private void updateAttackInput(
            final EntityManager entityManager,
            final Entity entity,
            final AttackAI ai,
            final Vector2d position
    ) {
        final var input = entityManager.getComponentOf(entity, WeaponInput.class).orElseThrow();
        final var attackAbility = entityManager.getComponentOf(entity, AttackAbility.class).orElseThrow();

        final var maybeTarget = ai.getAttackTarget();

        if (maybeTarget.isPresent()) {
            final var target = maybeTarget.get();
            final var targetPosition = entityManager.getComponentOf(target, Transform.class)
                                                    .orElseThrow().position;

            if (wantsAttackTarget(ai, position, targetPosition)) {
                input.attack = true;
                // FIXME: Take aim lead with noise
                attackAbility.targetPosition = targetPosition;
                return;
            }
        }

        input.attack = false;
    }

    private boolean wantsAttackTarget(
            final AttackAI ai,
            final Vector2d position,
            final Vector2d targetPosition
    ) {
        final var attackRangeSq = ai.attackRange * ai.attackRange;
        final var distanceSq = targetPosition.distanceSquared(position);
        return distanceSq <= attackRangeSq;
    }

    private static void findOrUpdateTarget(
            final EntityManager entityManager,
            final AttackAI ai,
            final Vector2d position
    ) {
        final var maybeTarget = ai.getAttackTarget();
        if (maybeTarget.isEmpty() || !isTargetValid(entityManager, maybeTarget.get(), position, ai)) {
            final var maybeNewTarget = findNewTarget(entityManager, ai, position);
            maybeNewTarget.ifPresentOrElse(ai::setAttackTarget, ai::clearAttackTarget);
        }
    }

    private static boolean isTargetValid(
            final EntityManager entityManager,
            final Entity target,
            final Vector2d position,
            final AttackAI ai
    ) {
        if (target.isMarkedForRemoval()) return false;
        if (!entityManager.hasComponent(target, Transform.class)) return false;
        if (!entityManager.hasComponent(target, ai.targetTagClass)) return false;

        final var targetPosition = entityManager.getComponentOf(target, Transform.class)
                                                .orElseThrow().position;
        return position.distanceSquared(targetPosition) <= ai.attackRange * ai.attackRange;
    }

    private static Optional<Entity> findNewTarget(
            final EntityManager entityManager,
            final AttackAI ai,
            final Vector2d position
    ) {
        LOG.debug("Getting entities with {}", ai.targetTagClass.getSimpleName());
        return entityManager
                .getEntitiesWith(ai.targetTagClass)
                .map(EntityManager.EntityComponentPair::getEntity)
                .filter(entity -> isTargetValid(entityManager, entity, position, ai))
                .findAny();
    }
}
