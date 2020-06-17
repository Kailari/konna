package fi.jakojaannos.roguelite.game.systems.physics;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.riista.ecs.annotation.Without;
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

            final var frictionVector = new Vector2d(velocity).normalize(physics.friction * delta);

            final var magnitudeX = Math.max(0, Math.abs(velocity.x) - Math.abs(frictionVector.x));
            final var magnitudeY = Math.max(0, Math.abs(velocity.y) - Math.abs(frictionVector.y));
            velocity.set(Math.signum(velocity.x) * magnitudeX,
                         Math.signum(velocity.y) * magnitudeY);
        });
    }

    public static record Resources(TimeManager timeManager) {}

    public static record EntityData(
            Physics physics,
            Velocity velocity,
            @Without InAir noInAir
    ) {}
}
