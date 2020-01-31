package fi.jakojaannos.roguelite.game.systems.physics;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.WalkingMovementAbility;
import fi.jakojaannos.roguelite.game.systems.SystemGroups;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

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
        val entityManager = world.getEntityManager();
        val delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        entities.forEach(entity -> {
            val physics = entityManager.getComponentOf(entity, Physics.class).orElseThrow();
            val velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            if (velocity.velocity.lengthSquared() == 0.0) return;

            val frictionVector = new Vector2d(velocity.velocity).normalize(physics.friction * delta);

            velocity.velocity.set(Math.signum(velocity.velocity.x) * Math.max(0, Math.abs(velocity.velocity.x) - Math.abs(frictionVector.x)),
                                  Math.signum(velocity.velocity.y) * Math.max(0, Math.abs(velocity.velocity.y) - Math.abs(frictionVector.y)));
        });
    }
}
