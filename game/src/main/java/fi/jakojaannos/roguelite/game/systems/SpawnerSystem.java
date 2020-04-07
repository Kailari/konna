package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.*;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;

public class SpawnerSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.EARLY_TICK)
                    .withComponent(SpawnerComponent.class)
                    .withComponent(Transform.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var delta = world.fetchResource(Time.class).getTimeStepInSeconds();
        final EntityManager cluster = world.getEntityManager();

        entities.forEach(entity -> {
            final var myPos = cluster.getComponentOf(entity, Transform.class).orElseThrow();
            final var spawnComp = cluster.getComponentOf(entity, SpawnerComponent.class).orElseThrow();

            spawnComp.spawnCoolDown -= delta;

            if (spawnComp.spawnCoolDown <= 0.0f) {
                spawnComp.spawnCoolDown = spawnComp.spawnFrequency;
                spawnComp.entityFactory.get(cluster, myPos, spawnComp);
            }
        });
    }
}
