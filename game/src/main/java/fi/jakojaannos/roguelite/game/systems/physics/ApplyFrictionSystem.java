package fi.jakojaannos.roguelite.game.systems.physics;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class ApplyFrictionSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .withComponent(Physics.class)
                    .withComponent(Velocity.class)
                    .withoutComponent(InAir.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        entities.forEach(entity -> {
            final var physics = entityManager.getComponentOf(entity, Physics.class).orElseThrow();
            final var velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            if (velocity.lengthSquared() == 0.0) return;

            final var frictionVector = new Vector2d(velocity).normalize(physics.friction * delta);

            final var magnitudeX = Math.max(0, Math.abs(velocity.x) - Math.abs(frictionVector.x));
            final var magnitudeY = Math.max(0, Math.abs(velocity.y) - Math.abs(frictionVector.y));
            velocity.set(Math.signum(velocity.x) * magnitudeX,
                         Math.signum(velocity.y) * magnitudeY);
        });
    }
}
