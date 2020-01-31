package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

@Slf4j
public class SlimeAIControllerSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.INPUT)
                    .requireResource(Players.class)
                    .withComponent(SlimeAI.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(MovementStats.class)
                    .withComponent(Transform.class)
                    .withComponent(Physics.class)
                    .withoutComponent(InAir.class);
    }

    private final Random random = new Random(123456);

    private final Vector2d
            emptyPos = new Vector2d(0.0, 0.0),
            tempTargetPos = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val timeManager = world.getOrCreateResource(Time.class);
        val optPlayer = world.getOrCreateResource(Players.class).getLocalPlayer();

        val playerPos = (optPlayer.isPresent())
                ? entityManager.getComponentOf(optPlayer.get(), Transform.class).orElseThrow().position
                : emptyPos;

        entities.forEach(entity -> {
            val ai = entityManager.getComponentOf(entity, SlimeAI.class).orElseThrow();
            val myPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow().position;

            if (timeManager.getCurrentGameTime() >= ai.lastJumpTimeStamp + ai.jumpCoolDownInTicks) {
                if (optPlayer.isPresent() && myPos.distanceSquared(playerPos) <= ai.chaseRadiusSquared) {
                    tempTargetPos.set(playerPos);
                } else {
                    tempTargetPos.set(myPos);
                    tempTargetPos.add(random.nextDouble() * 2.0 - 1.0,
                                      random.nextDouble() * 2.0 - 1.0);
                }
                hopTowardsPosition(entityManager, entity, timeManager, tempTargetPos);
            }
        });
    }

    private void hopTowardsPosition(
            EntityManager entityManager,
            Entity entity,
            TimeManager timeManager,
            Vector2d targetPos
    ) {
        val slimeAi = entityManager.getComponentOf(entity, SlimeAI.class).orElseThrow();
        val input = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
        val myPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow().position;
        val physics = entityManager.getComponentOf(entity, Physics.class).orElseThrow();

        /* Note: if a slime is very close to a player,
        it might be better for them to jump a shorter distance.
           Flight distance = velocity in air * time in air
             = jumpForce/mass * jumpDurationInTicks
           => jump duration = wanted distance / (force/mass)

           ==> newDuration = min(distance to player / (force/mass),

         */

        slimeAi.lastJumpTimeStamp = timeManager.getCurrentGameTime();
        input.move.set(0.0);

        targetPos.sub(myPos).normalize(slimeAi.jumpForce);
        physics.applyForce(targetPos);
        entityManager.addComponentTo(entity, new InAir(timeManager.getCurrentGameTime(), slimeAi.jumpDurationInTicks));
    }
}
