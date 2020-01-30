package fi.jakojaannos.roguelite.game.systems.cleanup;

import lombok.extern.slf4j.Slf4j;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

@Slf4j
public class ReaperSystem implements ECSSystem {
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
            LOG.debug(LogCategories.DEATH, "Destroyed a dead entity {}", entity.getId());
        });
    }
}
