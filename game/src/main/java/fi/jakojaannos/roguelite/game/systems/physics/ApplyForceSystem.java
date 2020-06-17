package fi.jakojaannos.roguelite.game.systems.physics;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;

public class ApplyForceSystem implements EcsSystem<EcsSystem.NoResources, ApplyForceSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final NoResources noResources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.forEach(entity -> {
            final var physics = entity.getData().physics;
            final var velocity = entity.getData().velocity;
            if (physics.acceleration.lengthSquared() > 0) {
                velocity.add(physics.acceleration);
            }
            physics.acceleration.set(0.0);
        });
    }

    public static record EntityData(Physics physics, Velocity velocity) {}
}
