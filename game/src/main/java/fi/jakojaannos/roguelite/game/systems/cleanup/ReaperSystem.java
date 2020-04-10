package fi.jakojaannos.roguelite.game.systems.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class ReaperSystem implements ECSSystem {
    private static final Logger LOG = LoggerFactory.getLogger(ReaperSystem.class);

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CLEANUP)
                    .withComponent(DeadTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        entities.forEach(entity -> {
            entityManager.destroyEntity(entity);
            LOG.trace(LogCategories.HEALTH, "Destroyed a dead entity {}", entity.getId());
        });
    }
}
