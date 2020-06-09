package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;

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
        final var entityManager = world.getEntityManager();
        final var time = world.fetchResource(TimeManager.class);

        entities.forEach(entity -> {
            final var inAir = entityManager.getComponentOf(entity, InAir.class).orElseThrow();

            if (time.getCurrentGameTime() > inAir.flightStartTimeStamp + inAir.flightDuration) {
                entityManager.removeComponentFrom(entity, InAir.class);
            }
        });
    }
}
