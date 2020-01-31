package fi.jakojaannos.roguelite.game.systems;

import lombok.extern.slf4j.Slf4j;
import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.StalkerAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;

@Slf4j
public class StalkerAIControllerSystem implements ECSSystem {
    private final Random random = new Random(8055); // 8055 = 666 + 420 + 6969
    private final Vector2d tempForce = new Vector2d();
    private final Vector2d emptyPos = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(StalkerAI.class)
                    .withComponent(CharacterInput.class)
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
        final var timeManager = world.getResource(Time.class);
        final var optPlayer = world.getOrCreateResource(Players.class).getLocalPlayer();
        final var playerPos = optPlayer.isPresent()
                ? entityManager.getComponentOf(optPlayer.get(), Transform.class).orElseThrow().position
                : emptyPos;

        entities.forEach(entity -> {
            final var stalkerAI = entityManager.getComponentOf(entity, StalkerAI.class).orElseThrow();
            final var characterInput = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
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
            final CharacterInput characterInput,
            final WalkingMovementAbility stats,
            final StalkerAI ai
    ) {
        stats.maxSpeed = ai.moveSpeedWalk;
        if (random.nextInt(40) == 0) {
            characterInput.move.set(
                    random.nextDouble() * 2 - 1.0,
                    random.nextDouble() * 2 - 1.0
            );
        }
    }

    public void doCloseRangeMovement(
            final CharacterInput characterInput,
            final EntityManager entityManager,
            final Entity entity,
            final WalkingMovementAbility stats,
            final Physics physics,
            final StalkerAI ai,
            final Time time,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        if (time.getCurrentGameTime() > ai.lastJumpTimeStamp + ai.jumpCoolDownInTicks) {
            leapTowardsPlayer(characterInput, entityManager, entity, physics, ai, time, myPos, playerPos);
        } else {
            stats.maxSpeed = ai.moveSpeedWalk;
            characterInput.move.set(playerPos).sub(myPos);
        }
    }

    public void leapTowardsPlayer(
            final CharacterInput characterInput,
            final EntityManager entityManager,
            final Entity entity,
            final Physics physics,
            final StalkerAI ai,
            final Time time,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        characterInput.move.set(0.0);
        ai.lastJumpTimeStamp = time.getCurrentGameTime();
        entityManager.addComponentTo(entity, new InAir(time.getCurrentGameTime(), ai.jumpDurationInTicks));
        tempForce.set(playerPos)
                 .sub(myPos)
                 .normalize(ai.moveSpeedJump);
        physics.applyForce(tempForce);
    }

    public void sneakTowardsPlayer(
            final CharacterInput characterInput,
            final WalkingMovementAbility stats,
            final StalkerAI ai,
            final Vector2d myPos,
            final Vector2d playerPos
    ) {
        stats.maxSpeed = ai.moveSpeedSneak;
        characterInput.move.set(playerPos).sub(myPos);
    }

}
