package fi.jakojaannos.roguelite.game.systems.cleanup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.LogCategories;
import fi.jakojaannos.roguelite.game.data.components.character.DeadTag;

public class ReaperSystem implements EcsSystem<EcsSystem.NoResources, ReaperSystem.EntityData, EcsSystem.NoEvents> {
    private static final Logger LOG = LoggerFactory.getLogger(ReaperSystem.class);

    @Override
    public void tick(
            final NoResources noResources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> {
            entity.destroy();
            LOG.trace(LogCategories.HEALTH, "Destroyed a dead entity {}", entity.getId());
        });
    }

    public static record EntityData(DeadTag deadTag) {}
}
