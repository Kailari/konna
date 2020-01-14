package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import lombok.val;

import java.util.stream.Stream;

public class HandleEntitiesInAirSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .withComponent(InAir.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val time = world.getOrCreateResource(Time.class);

        entities.forEach(entity -> {
            val inAir = entityManager.getComponentOf(entity, InAir.class).orElseThrow();

            if (time.getCurrentGameTime() >= inAir.flightStartTimeStamp + inAir.flightDuration) {
                entityManager.removeComponentFrom(entity, InAir.class);
            }
        });
    }
}
