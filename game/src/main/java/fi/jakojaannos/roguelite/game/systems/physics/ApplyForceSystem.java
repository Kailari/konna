package fi.jakojaannos.roguelite.game.systems.physics;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;

public class ApplyForceSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.PHYSICS_TICK)
                    .tickBefore(ApplyVelocitySystem.class)
                    .withComponent(Physics.class)
                    .withComponent(Velocity.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();

        entities.forEach(entity -> {
            final var physics = entityManager.getComponentOf(entity, Physics.class).orElseThrow();
            final var velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            velocity.add(physics.acceleration);
            physics.acceleration.set(0.0);
        });
    }
}
