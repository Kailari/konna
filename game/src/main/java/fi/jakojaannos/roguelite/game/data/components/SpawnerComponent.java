package fi.jakojaannos.roguelite.game.data.components;

import org.joml.Vector2d;

import java.util.Random;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.data.resources.Entities;

public class SpawnerComponent {
    public final Random random;
    public final EntityFactory entityFactory;
    public long timeBetweenSpawns;
    public long spawnTimestamp;
    public double maxSpawnDistance;

    public SpawnerComponent(final long timeBetweenSpawns, final EntityFactory entityFactory) {
        this(timeBetweenSpawns, entityFactory, 3.0f, System.nanoTime());
    }

    public SpawnerComponent(
            final long timeBetweenSpawns,
            final EntityFactory entityFactory,
            final double maxSpawnDistance,
            final long seed
    ) {
        this.timeBetweenSpawns = timeBetweenSpawns;
        this.maxSpawnDistance = maxSpawnDistance;
        this.entityFactory = entityFactory;

        this.spawnTimestamp = -timeBetweenSpawns;
        this.random = new Random(seed);
    }

    private static Vector2d getRandomSpotAround(
            final Transform origin,
            final double maxDist,
            final Random random
    ) {
        final double xDir = random.nextDouble() * 2.0f - 1.0f;
        final double yDir = random.nextDouble() * 2.0f - 1.0f;
        final var result = new Vector2d(xDir, yDir);
        if (result.lengthSquared() != 0.0) {
            result.normalize();
        }

        result.mul(maxDist * random.nextDouble());
        result.add(origin.position);

        return result;
    }

    public interface EntityFactory {
        static EntityFactory withRandomDistance(final EntityFactory factory) {
            return (entityManager, spawnerTransform, spawnerComponent) -> {
                final var randomPosition = getRandomSpotAround(spawnerTransform,
                                                               spawnerComponent.maxSpawnDistance,
                                                               spawnerComponent.random);

                return factory.get(entityManager, new Transform(randomPosition.x, randomPosition.y), spawnerComponent);
            };

        }

        EntityHandle get(Entities entities, Transform spawnerTransform, SpawnerComponent spawnerComponent);
    }
}
