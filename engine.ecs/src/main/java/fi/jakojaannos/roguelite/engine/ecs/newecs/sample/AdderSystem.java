package fi.jakojaannos.roguelite.engine.ecs.newecs.sample;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.newecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.newecs.Requirements;

public class AdderSystem implements EcsSystem<AdderSystem.Resources, AdderSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public Requirements<Resources, EntityData, NoEvents> declareRequirements(
            final Requirements.Builder<Resources, EntityData, NoEvents> require
    ) {
        return require.entityData(EntityData.class)
                      .resources(Resources.class)
                      .build();
    }

    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents events
    ) {
        entities.forEach(entity -> {
            final var componentA = entity.getData().a;
            final var componentB = entity.getData().b;

            componentA.value += componentB.value * resources.multiplier().value;
        });
    }

    public static record EntityData(
            ValueComponent a,
            AmountComponent b
    ) {
    }

    public static record Resources(
            Multiplier multiplier
    ) {
    }
}

