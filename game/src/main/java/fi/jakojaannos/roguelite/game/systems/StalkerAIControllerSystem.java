package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;

public class StalkerAIControllerSystem implements ECSSystem {
    private final Random random = new Random(8055); // 8055 = 666 + 420 + 6969
    private final Vector2d tempForce = new Vector2d();
    private final Vector2d emptyPos = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(StalkerAI.class)
                    .withComponent(MovementInput.class)
                    .withComponent(Transform.class)
                    .withComponent(WalkingMovementAbility.class)
                    .withComponent(Physics.class)
                    .withoutComponent(InAir.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var timeManager = world.fetchResource(TimeManager.class);
        final var optPlayer = world.fetchResource(Players.class).getLocalPlayer();
        final var playerPos = optPlayer.flatMap(player -> player.getComponent(Transform.class))
                                       .map(transform -> transform.position)
                                       .orElse(this.emptyPos);

        entities.forEach(entity -> {
            final var stalkerAI = entityManager.getComponentOf(entity, StalkerAI.class).orElseThrow();
            final var characterInput = entityManager.getComponentOf(entity, MovementInput.class).orElseThrow();
            final var characterStats = entityManager.getComponentOf(entity, WalkingMovementAbility.class).orElseThrow();
            final var physics = entityManager.getComponentOf(entity, Physics.class).orElseThrow();
            final var myPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();

            if (optPlayer.isEmpty()) {
                doWanderMovement(characterInput, characterStats, stalkerAI);
                return;
            }

            final var distToPlayerSquared = myPos.position.distanceSquared(playerPos);

            if (distToPlayerSquared <= stalkerAI.leapRadiusSquared) {
                doCloseRangeMovement(characterInput,
                                     entityManager,
                                     entity,
                                     characterStats,
                                     physics,
                                     stalkerAI,
                                     timeManager,
                                     myPos.position,
                                     playerPos);
            } else if (distToPlayerSquared <= stalkerAI.aggroRadiusSquared) {
                sneakTowardsPlayer(characterInput, characterStats, stalkerAI, myPos.position, playerPos);
            } else {
                doWanderMovement(characterInput, characterStats, stalkerAI);
            }
        });
    }

    public void doWanderMovement(
            final MovementInput movementInput,
            final WalkingMovementAbility stats,
            final StalkerAI ai
    ) {
        stats.maxSpeed = ai.moveSpeedWalk;
        if (this.random.nextInt(40) == 0) {
            movementInput.move.set(
                    this.random.nextDouble() * 2 - 1.0,
                    this.random.nextDouble() * 2 - 1.0
            );
        }
    }

    public void doCloseRangeMovement(
            final MovementInput movementInput,
            final EntityManager entityManager,
            final Entity entity,
            final WalkingMovementAbility stats,
            final Physics physics,
            final StalkerAI ai,
            final TimeManager time,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        if (time.getCurrentGameTime() > ai.lastJumpTimeStamp + ai.jumpCoolDownInTicks) {
            leapTowardsPlayer(movementInput, entityManager, entity, physics, ai, time, myPos, playerPos);
        } else {
            stats.maxSpeed = ai.moveSpeedWalk;
            movementInput.move.set(playerPos).sub(myPos);
        }
    }

    public void leapTowardsPlayer(
            final MovementInput movementInput,
            final EntityManager entityManager,
            final Entity entity,
            final Physics physics,
            final StalkerAI ai,
            final TimeManager time,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        movementInput.move.set(0.0);
        ai.lastJumpTimeStamp = time.getCurrentGameTime();
        entityManager.addComponentTo(entity, new InAir(time.getCurrentGameTime(), ai.jumpDurationInTicks));
        this.tempForce.set(playerPos)
                      .sub(myPos)
                      .normalize(ai.moveSpeedJump);
        physics.applyForce(this.tempForce);
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

}
