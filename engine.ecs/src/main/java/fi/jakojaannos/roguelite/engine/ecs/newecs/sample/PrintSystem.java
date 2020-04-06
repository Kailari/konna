package fi.jakojaannos.roguelite.engine.ecs.newecs.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.newecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.newecs.Requirements;

public class PrintSystem implements EcsSystem<EcsSystem.NoResources, PrintSystem.EntityData, EcsSystem.NoEvents> {
    private static final Logger LOG = LoggerFactory.getLogger(PrintSystem.class);

    @Override
    public Requirements<NoResources, EntityData, NoEvents> declareRequirements(
            final Requirements.Builder<NoResources, EntityData, NoEvents> require
    ) {
        return require.entityData(EntityData.class)
                      .build();
    }

    @Override
    public void tick(
            final NoResources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents events
    ) {
        entities.forEach(entity -> {
            final var componentA = entity.getData().a;
            LOG.info("Value for entity #{}: {}", entity.getId(), componentA.value);
        });
    }

    public static record EntityData(ValueComponent a) {
    }
}

