package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.SlimeAI;
import fi.jakojaannos.roguelite.game.data.components.SlimeSharedAI;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
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
            final Stream<Entity> entities,
            final World world
    ) {

        val entityManager = world.getEntityManager();

        entities.forEach(entity -> {

            val optSharedAi = entityManager.getComponentOf(entity, SlimeSharedAI.class);
            if (optSharedAi.isPresent()) {
                val sharedAi = optSharedAi.get();
                sharedAi.slimes.remove(entity);
                entityManager.removeComponentIfPresent(entity, SlimeSharedAI.class);
            }


            val ai = entityManager.getComponentOf(entity, SlimeAI.class).get();
            if (ai.slimeSize <= 1) return;


            val pos = entityManager.getComponentOf(entity, Transform.class).get();
            val sharedAi = new SlimeSharedAI();

            for (int i = 0; i < 4; i++) {
                double xSpread = random.nextDouble() * 2.0 - 1.0;
                double ySpread = random.nextDouble() * 2.0 - 1.0;

                tempDir.set(xSpread, ySpread);
                if (tempDir.lengthSquared() != 0.0) {
                    tempDir.normalize();
                }


                if (ai.slimeSize == 3) {
                    val slime = SlimeArchetype.createMediumSlimeWithInitialVelocity(
                            entityManager,
                            pos.getCenterX() + xSpread,
                            pos.getCenterY() + ySpread,
                            tempDir,
                            0.4
                    );
                    entityManager.addComponentTo(slime, sharedAi);
                    sharedAi.slimes.add(slime);

                } else if (ai.slimeSize == 2) {
                    val slime = SlimeArchetype.createSmallSlimeWithInitialVelocity(
                            entityManager,
                            pos.getCenterX() + xSpread,
                            pos.getCenterY() + ySpread,
                            tempDir,
                            0.25
                    );
                    entityManager.addComponentTo(slime, sharedAi);
                    sharedAi.slimes.add(slime);

                }
            }


        });

    }
}
