package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeSharedAI;
import fi.jakojaannos.roguelite.game.systems.cleanup.ReaperSystem;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

@Slf4j
public class SlimeDeathHandlerSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(HealthUpdateSystem.class)
                    .tickBefore(ReaperSystem.class)
                    .withComponent(DeadTag.class)
                    .withComponent(SlimeAI.class)
                    .withComponent(Transform.class);

    }

    private final Random random = new Random(System.nanoTime());
    private final Vector2d tempDir = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val timeManager = world.getOrCreateResource(Time.class);

        entities.forEach(entity -> {
            val ai = entityManager.getComponentOf(entity, SlimeAI.class).orElseThrow();
            val myPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            val optSharedAi = entityManager.getComponentOf(entity, SlimeSharedAI.class);

            if (optSharedAi.isPresent()) {
                val sharedAi = optSharedAi.get();
                sharedAi.slimes.remove(entity);
                entityManager.removeComponentIfPresent(entity, SlimeSharedAI.class);
            }

            if (ai.offspringAmountAfterDeath <= 1) return;

            final double childSize = ai.slimeSize / ai.offspringAmountAfterDeath;
            if (childSize < 0.75) return;

            for (int i = 0; i < ai.offspringAmountAfterDeath; i++) {
                double xSpread = random.nextDouble() * 2.0 - 1.0;
                double ySpread = random.nextDouble() * 2.0 - 1.0;
                double force = random.nextDouble() * 5.0 + 1.0;
                int flightDur = random.nextInt(7) + 7;

                tempDir.set(xSpread, ySpread);
                if (tempDir.lengthSquared() != 0.0) {
                    tempDir.normalize(force);
                }

                Entity child = SlimeArchetype.createSlimeOfSize(
                        entityManager,
                        myPos.position.x,
                        myPos.position.y,
                        childSize
                );

                entityManager.getComponentOf(child, Physics.class)
                             .ifPresent(physics -> {
                                 physics.applyForce(tempDir.mul(physics.mass));
                             });

                entityManager.addComponentTo(child, new InAir(timeManager.getCurrentGameTime(), flightDur));
            }
        });
    }
}
