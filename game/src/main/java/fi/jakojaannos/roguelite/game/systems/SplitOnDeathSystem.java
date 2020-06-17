package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.Random;
import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.resources.Entities;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.SlimeArchetype;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeSharedAI;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SplitOnDeath;

public class SplitOnDeathSystem implements EcsSystem<SplitOnDeathSystem.Resources, SplitOnDeathSystem.EntityData, EcsSystem.NoEvents> {
    private final Random random = new Random(System.nanoTime());

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var timeManager = resources.timeManager;

        entities.forEach(entity -> {
            final var split = entity.getData().splitOnDeath;
            final var transform = entity.getData().transform;
            entity.getComponent(SlimeSharedAI.class)
                  .ifPresent(sharedAi -> {
                      sharedAi.slimes.remove(entity.getHandle());
                      entity.removeComponent(SlimeSharedAI.class);
                  });

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
                final var flightDuration = getFlightDuration(split);

                final var direction = new Vector2d(xSpread, ySpread);
                if (direction.lengthSquared() != 0.0) {
                    direction.normalize(force);
                }

                // FIXME: Add field to component for defining the factory
                final var child = SlimeArchetype.createSlimeOfSize(resources.entities,
                                                                   transform,
                                                                   childSize);

                child.getComponent(Physics.class)
                     .ifPresent(physics -> physics.applyForce(direction.mul(physics.mass)));

                child.addComponent(new InAir(timeManager.getCurrentGameTime(), flightDuration));
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

    public static record Resources(
            Entities entities,
            TimeManager timeManager
    ) {}

    public static record EntityData(
            Transform transform,
            SplitOnDeath splitOnDeath,
            DeadTag deadTag
    ) {}
}
