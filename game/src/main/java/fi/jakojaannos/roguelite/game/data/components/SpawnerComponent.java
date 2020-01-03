package fi.jakojaannos.roguelite.game.data.components;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import lombok.val;
import org.joml.Vector2d;

import java.util.Random;

public class SpawnerComponent implements Component {
    public double spawnFrequency;
    public double spawnCoolDown, maxSpawnDistance;

    public final Random random;
    public final EntityFactory entityFactory;

    private final Vector2d temp = new Vector2d();

    public SpawnerComponent(double spawnFrequency, EntityFactory entityFactory) {
        this(spawnFrequency, entityFactory, 3.0f, System.currentTimeMillis());
    }

    public SpawnerComponent(
            double spawnFrequency,
            EntityFactory entityFactory,
            double maxSpawnDistance,
            long seed
    ) {
        this.spawnFrequency = spawnFrequency;
        this.maxSpawnDistance = maxSpawnDistance;
        this.entityFactory = entityFactory;

        this.spawnCoolDown = spawnFrequency;
        this.random = new Random(seed);
    }

    private static Vector2d getRandomSpotAround(
            final Transform origin,
            final double maxDist,
            final Random random,
            final Vector2d result
    ) {
        double xDir = random.nextDouble() * 2.0f - 1.0f;
        double yDir = random.nextDouble() * 2.0f - 1.0f;
        result.set(xDir, yDir);
        if (result.lengthSquared() != 0.0) result.normalize();

        result.mul(maxDist * random.nextDouble());
        result.add(origin.position);

        return result;
    }


    public interface EntityFactory {
        static EntityFactory withRandomDistance(final EntityFactory factory) {
            return (entityManager, spawnerTransform, spawnerComponent) -> {
                val randomPosition = getRandomSpotAround(spawnerTransform,
                                                         spawnerComponent.maxSpawnDistance,
                                                         spawnerComponent.random,
                                                         spawnerComponent.temp);

                return factory.get(entityManager, new Transform(randomPosition.x, randomPosition.y), spawnerComponent);
            };

        }

        Entity get(
                EntityManager entityManager,
                Transform spawnerPos,
                SpawnerComponent spawnerComponent
        );
    }

}
