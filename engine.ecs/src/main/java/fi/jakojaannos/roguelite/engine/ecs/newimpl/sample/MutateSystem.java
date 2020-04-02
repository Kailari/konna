package fi.jakojaannos.roguelite.engine.ecs.newimpl.sample;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.Requirements;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.systemdata.RequirementsImpl;

public class MutateSystem implements EcsSystem<EcsSystem.NoResources, MutateSystem.EntityData, EcsSystem.NoEvents> {
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
            final var componentB = entity.getData().b;

            componentA.value += componentB.value;
        });
    }

    public static record EntityData(
            SampleComponentA a,
            SampleComponentB b
    ) {
    }
}

