package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.CharacterStats;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

@Slf4j
public class CharacterMovementSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .withComponent(Transform.class)
                    .withComponent(Velocity.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(CharacterStats.class);
    }

    private static final float INPUT_EPSILON = 0.001f;

    private final Vector2d tmpVelocity = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        entities.forEach(entity -> {
            val input = world.getEntityManager().getComponentOf(entity, CharacterInput.class).orElseThrow();
            val stats = world.getEntityManager().getComponentOf(entity, CharacterStats.class).orElseThrow();
            val velocity = world.getEntityManager().getComponentOf(entity, Velocity.class).orElseThrow();

            // Accelerate
            if (input.move.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON) {
                input.move.normalize(stats.acceleration * delta, tmpVelocity);
                tmpVelocity.add(velocity.velocity);

                if (tmpVelocity.lengthSquared() > stats.speed * stats.speed) {
                    tmpVelocity.normalize(stats.speed);
                }
                velocity.velocity.set(tmpVelocity);
            }
            // Deceleration
            else {
                val decelerationThisFrame = stats.friction * (float) delta;
                val xVel = velocity.velocity.x;
                val yVel = velocity.velocity.y;
                velocity.velocity.set(Math.signum(xVel) * Math.max(0.0f, Math.abs(xVel) - decelerationThisFrame),
                                      Math.signum(yVel) * Math.max(0.0f, Math.abs(yVel) - decelerationThisFrame));
            }
        });
    }
}
