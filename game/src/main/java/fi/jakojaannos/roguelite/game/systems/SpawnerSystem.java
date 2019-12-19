package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.game.data.components.SpawnerComponent;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class SpawnerSystem implements ECSSystem {
    @Override
    public void declareRequirements( RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.EARLY_TICK)
                    .withComponent(SpawnerComponent.class)
                    .withComponent(Transform.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();
        EntityManager cluster = world.getEntityManager();

        entities.forEach(entity -> {
            val myPos = cluster.getComponentOf(entity, Transform.class).get();
            val spawnComp = cluster.getComponentOf(entity, SpawnerComponent.class).get();

            spawnComp.spawnCoolDown -= delta;

            if (spawnComp.spawnCoolDown <= 0.0f) {
                spawnComp.spawnCoolDown = spawnComp.spawnFrequency;
                spawnComp.entityFactory.get(cluster, myPos, spawnComp);
            }
        });
    }
}
