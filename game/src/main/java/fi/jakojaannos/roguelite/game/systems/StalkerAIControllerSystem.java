package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.Without;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;

public class StalkerAIControllerSystem implements EcsSystem<StalkerAIControllerSystem.Resources, StalkerAIControllerSystem.EntityData, EcsSystem.NoEvents> {
    private final Random random = new Random(8055); // 8055 = 666 + 420 + 6969
    private final Vector2d tempForce = new Vector2d();
    private final Vector2d emptyPos = new Vector2d();

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var maybePlayer = resources.players.getLocalPlayer();
        if (maybePlayer.isEmpty()) {
            entities.forEach(this::doWanderMovement);
            return;
        }

        final var playerPos = maybePlayer.flatMap(player -> player.getComponent(Transform.class))
                                         .map(transform -> transform.position)
                                         .orElse(this.emptyPos);

        entities.forEach(entity -> {
            final var ai = entity.getData().stalkerAi;
            final var input = entity.getData().input;
            final var ability = entity.getData().ability;
            final var transform = entity.getData().transform;

            final var distToPlayerSquared = transform.position.distanceSquared(playerPos);

            if (distToPlayerSquared <= ai.leapRadiusSquared) {
                doCloseRangeMovement(entity,
                                     ai,
                                     resources.timeManager,
                                     transform.position,
                                     playerPos);
            } else if (distToPlayerSquared <= ai.aggroRadiusSquared) {
                sneakTowardsPlayer(input, ability, ai, transform.position, playerPos);
            } else {
                doWanderMovement(entity);
            }
        });
    }

    public void doWanderMovement(final EntityDataHandle<EntityData> entity) {
        final var ability = entity.getData().ability;
        final var ai = entity.getData().stalkerAi;
        final var input = entity.getData().input;

        ability.maxSpeed = ai.moveSpeedWalk;
        if (this.random.nextInt(40) == 0) {
            input.move.set(this.random.nextDouble() * 2 - 1.0,
                           this.random.nextDouble() * 2 - 1.0);
        }
    }

    public void doCloseRangeMovement(
            final EntityDataHandle<EntityData> entity,
            final StalkerAI ai,
            final TimeManager time,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        if (time.getCurrentGameTime() > ai.lastJumpTimeStamp + ai.jumpCoolDownInTicks) {
            leapTowardsPlayer(entity, ai, time, myPos, playerPos);
        } else {
            entity.getData().ability.maxSpeed = ai.moveSpeedWalk;
            entity.getData().input.move.set(playerPos).sub(myPos);
        }
    }

    public void leapTowardsPlayer(
            final EntityDataHandle<EntityData> entity,
            final StalkerAI ai,
            final TimeManager time,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        entity.getData().input.move.set(0.0);
        ai.lastJumpTimeStamp = time.getCurrentGameTime();
        entity.addComponent(new InAir(time.getCurrentGameTime(), ai.jumpDurationInTicks));
        this.tempForce.set(playerPos)
                      .sub(myPos)
                      .normalize(ai.moveSpeedJump);
        entity.getData().physics.applyForce(this.tempForce);
    }

    public void sneakTowardsPlayer(
            final MovementInput movementInput,
            final WalkingMovementAbility stats,
            final StalkerAI ai,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        stats.maxSpeed = ai.moveSpeedSneak;
        movementInput.move.set(playerPos).sub(myPos);
    }

    public static record Resources(
            Players players,
            TimeManager timeManager
    ) {}

    public static record EntityData(
            StalkerAI stalkerAi,
            MovementInput input,
            WalkingMovementAbility ability,
            Transform transform,
            Physics physics,
            @Without InAir notInAir
    ) {}
}
