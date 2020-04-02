package fi.jakojaannos.roguelite.engine.ecs.newimpl.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.Requirements;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.systemdata.RequirementsImpl;

public class PrintSystem implements EcsSystem<EcsSystem.NoResources, PrintSystem.EntityData, EcsSystem.NoEvents> {
    private static final Logger LOG = LoggerFactory.getLogger(PrintSystem.class);

    @Override
    public Requirements<EntityData> declareRequirements() {
        final var require = new RequirementsImpl<EntityData>();
        require.entityData(EntityData.class);
        return require;
    }

    @Override
    public void tick(
            final NoResources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents events
    ) {
        entities.forEach(entity -> {
            final var componentA = entity.getData().a;

            LOG.info("Component A: {}", componentA.value);
        });
    }

    public static record EntityData(SampleComponentA a) {
    }
}

