package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeSharedAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;
import fi.jakojaannos.roguelite.game.systems.cleanup.ReaperSystem;

public class SplitOnDeathSystem implements ECSSystem {
    private final Random random = new Random(System.nanoTime());
    private final Vector2d tempDir = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.tickAfter(HealthUpdateSystem.class)
                    .tickBefore(ReaperSystem.class)
                    .withComponent(DeadTag.class)
                    .withComponent(SplitOnDeath.class)
                    .withComponent(Transform.class);

    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var timeManager = world.fetchResource(TimeManager.class);

        entities.forEach(entity -> {
            final var split = entityManager.getComponentOf(entity, SplitOnDeath.class).orElseThrow();
            final var myPos = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            final var maybeSharedAi = entityManager.getComponentOf(entity, SlimeSharedAI.class);

            if (maybeSharedAi.isPresent()) {
                final var sharedAi = maybeSharedAi.get();
                sharedAi.slimes.remove(entity);
                entityManager.removeComponentIfPresent(entity, SlimeSharedAI.class);
            }

            if (split.offspringAmount <= 1) {
                return;
            }

            final var preservedSizeRatio = 1.0 - split.sizeLossPercentage;
            final var preservedSize = split.size * preservedSizeRatio;
            final var childSize = preservedSize / split.offspringAmount;
            if (childSize < 0.75) {
                return;
            }

            for (int i = 0; i < split.offspringAmount; i++) {
                final var xSpread = this.random.nextDouble() * 2.0 - 1.0;
                final var ySpread = this.random.nextDouble() * 2.0 - 1.0;
                final var force = getSpawnForce(split);
                final var flightDur = getFlightDuration(split);

                this.tempDir.set(xSpread, ySpread);
                if (this.tempDir.lengthSquared() != 0.0) {
                    this.tempDir.normalize(force);
                }

                // FIXME: Add field to component for defining the factory
                final Entity child = SlimeArchetype.createSlimeOfSize(entityManager,
                                                                      myPos.position.x,
                                                                      myPos.position.y,
                                                                      childSize);

                entityManager.getComponentOf(child, Physics.class)
                             .ifPresent(physics -> physics.applyForce(this.tempDir.mul(physics.mass)));

                entityManager.addComponentTo(child, new InAir(timeManager.getCurrentGameTime(), flightDur));
            }
        });
    }

    private double getSpawnForce(final SplitOnDeath split) {
        return this.random.nextDouble() * (split.maxSpawnForce - split.minSpawnForce) + split.minSpawnForce;
    }

    private int getFlightDuration(final SplitOnDeath split) {
        final var bound = split.maxSpawnFlightDurationInTicks - split.minSpawnFlightDurationInTicks;
        return this.random.nextInt(bound) + split.minSpawnFlightDurationInTicks;
    }
}
