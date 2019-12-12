package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.SlimeAI;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;
import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

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
            Stream<Entity> entities, World world, double delta
    ) {

        val entityManager = world.getEntityManager();

        entities.forEach(entity -> {

            val ai = entityManager.getComponentOf(entity, SlimeAI.class).get();
            if (ai.slimeSize <= 1) return;


            val pos = entityManager.getComponentOf(entity, Transform.class).get();

            for (int i = 0; i < 4; i++) {
                double xSpread = random.nextDouble() * 2.0 - 1.0;
                double ySpread = random.nextDouble() * 2.0 - 1.0;

                tempDir.set(xSpread, ySpread);
                if (tempDir.lengthSquared() != 0.0) {
                    tempDir.normalize();
                }


                if (ai.slimeSize == 3) {
                    SlimeArchetype.createMediumSlimeWithInitialVelocity(
                            entityManager,
                            pos.getCenterX() + xSpread,
                            pos.getCenterY() + ySpread,
                            tempDir,
                            0.4
                    );

                } else if (ai.slimeSize == 2) {
                    SlimeArchetype.createSmallSlimeWithInitialVelocity(
                            entityManager,
                            pos.getCenterX() + xSpread,
                            pos.getCenterY() + ySpread,
                            tempDir,
                            0.25
                    );
                }
            }


        });

    }
}
