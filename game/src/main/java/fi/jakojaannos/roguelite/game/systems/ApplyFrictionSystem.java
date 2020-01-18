package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

public class ApplyFrictionSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .withComponent(MovementStats.class)
                    .withComponent(Velocity.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        entities.forEach(entity -> {
            val stats = entityManager.getComponentOf(entity, MovementStats.class).orElseThrow();
            val velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            if (velocity.velocity.lengthSquared() == 0.0) return;

            val frictionVector = new Vector2d(velocity.velocity).normalize(stats.friction * delta);

            velocity.velocity.set(Math.signum(velocity.velocity.x) * Math.max(0, Math.abs(velocity.velocity.x) - Math.abs(frictionVector.x)),
                                  Math.signum(velocity.velocity.y) * Math.max(0, Math.abs(velocity.velocity.y) - Math.abs(frictionVector.y)));
        });
    }
}
