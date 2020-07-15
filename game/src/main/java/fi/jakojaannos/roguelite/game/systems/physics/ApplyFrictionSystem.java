package fi.jakojaannos.roguelite.game.systems.physics;

import org.joml.Vector2d;

import java.util.Optional;
import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;

public class ApplyFrictionSystem implements EcsSystem<ApplyFrictionSystem.Resources, ApplyFrictionSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var delta = resources.timeManager.getTimeStepInSeconds();

        entities.forEach(entity -> {
            final var physics = entity.getData().physics;
            final var velocity = entity.getData().velocity;

            if (velocity.lengthSquared() == 0.0) {
                return;
            }

            final double airFriction = velocity.lengthSquared() * physics.drag * delta;
            final double groundFriction = entity.getData().inAir.isEmpty()
                    ? physics.friction * delta
                    : 0.0;

            applyFriction(velocity, airFriction + groundFriction);
        });
    }

    private static void applyFriction(
            final Velocity velocity,
            final double frictionAmount
    ) {
        if (frictionAmount == 0.0) {
            return;
        }
        if (frictionAmount * frictionAmount >= velocity.lengthSquared()) {
            velocity.set(0.0, 0.0);
            return;
        }

        final var frictionVector = new Vector2d(velocity).normalize(frictionAmount);
        velocity.sub(frictionVector);
    }

    public static record Resources(TimeManager timeManager) {}

    public static record EntityData(
            Physics physics,
            Velocity velocity,
            Optional<InAir>inAir
    ) {}
}
