package fi.jakojaannos.roguelite.game.systems;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;

@Slf4j
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
        final var delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();
        final EntityManager cluster = world.getEntityManager();

        entities.forEach(entity -> {
            final var myPos = cluster.getComponentOf(entity, Transform.class).get();
            final var spawnComp = cluster.getComponentOf(entity, SpawnerComponent.class).get();

            spawnComp.spawnCoolDown -= delta;

            if (spawnComp.spawnCoolDown <= 0.0f) {
                spawnComp.spawnCoolDown = spawnComp.spawnFrequency;
                spawnComp.entityFactory.get(cluster, myPos, spawnComp);
            }
        });
    }
}
