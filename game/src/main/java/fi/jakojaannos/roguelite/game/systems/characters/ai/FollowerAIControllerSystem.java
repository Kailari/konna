package fi.jakojaannos.roguelite.game.systems.characters.ai;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.FollowerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;

public class FollowerAIControllerSystem implements EcsSystem<FollowerAIControllerSystem.Resources, FollowerAIControllerSystem.EntityData, EcsSystem.NoEvents> {
    private final Random random = new Random(123456);

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var maybePlayer = resources.players.getLocalPlayer();
        final var playerPosition = maybePlayer.flatMap(player -> player.getComponent(Transform.class))
                                              .map(transform -> transform.position)
                                              .orElse(new Vector2d(0.0));

        entities.forEach(entity -> {
            final var ai = entity.getData().ai;
            final var input = entity.getData().input;
            final var entityPosition = entity.getData().transform.position;

            if (maybePlayer.isPresent() && wantsMoveTowardsTarget(ai, playerPosition, entityPosition)) {
                input.move.set(playerPosition)
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

    public static record Resources(Players players) {}

    public static record EntityData(
            FollowerAI ai,
            MovementInput input,
            Transform transform
    ) {}
}
