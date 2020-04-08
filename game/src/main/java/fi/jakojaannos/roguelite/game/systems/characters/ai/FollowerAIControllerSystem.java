package fi.jakojaannos.roguelite.game.systems.characters.ai;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class FollowerAIControllerSystem implements ECSSystem {
    private final Random random = new Random(123456);

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(FollowerAI.class)
                    .withComponent(MovementInput.class)
                    .withComponent(Transform.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var maybePlayer = world.fetchResource(Players.class).getLocalPlayer();
        final var playerPos = maybePlayer.map(player -> entityManager.getComponentOf(player, Transform.class)
                                                                     .orElseThrow().position)
                                         .orElse(new Vector2d(0.0));

        entities.forEach(entity -> {
            final var ai = entityManager.getComponentOf(entity, FollowerAI.class)
                                        .orElseThrow();
            final var input = entityManager.getComponentOf(entity, MovementInput.class)
                                           .orElseThrow();
            final var entityPosition = entityManager.getComponentOf(entity, Transform.class)
                                                    .orElseThrow().position;

            if (maybePlayer.isPresent() && wantsMoveTowardsTarget(ai, playerPos, entityPosition)) {
                input.move.set(playerPos)
                          .sub(entityPosition);

            } else {
                input.move.set(this.random.nextDouble() * 2.0 - 1.0,
                               this.random.nextDouble() * 2.0 - 1.0);
            }

            if (input.move.lengthSquared() != 0.0) {
                input.move.normalize();
            } else {
                input.move.set(0.0);
            }
        });
    }

    private boolean wantsMoveTowardsTarget(
            final FollowerAI ai,
            final Vector2d targetPos,
            final Vector2d ownPosition
    ) {
        final var distanceToTarget = ownPosition.distanceSquared(targetPos);

        final var isWithinAggroRadius = distanceToTarget <= ai.aggroRadius * ai.aggroRadius;
        final var isOutsideTargetRadius = distanceToTarget >= ai.targetDistance * ai.targetDistance;
        return isWithinAggroRadius && isOutsideTargetRadius;
    }

}
