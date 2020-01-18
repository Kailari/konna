package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.InAir;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.stream.Stream;

@Slf4j
public class CharacterMovementSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.CHARACTER_TICK)
                    .tickAfter(ApplyFrictionSystem.class)
                    .withComponent(Transform.class)
                    .withComponent(Velocity.class)
                    .withComponent(CharacterInput.class)
                    .withComponent(MovementStats.class)
                    .withoutComponent(InAir.class);
    }

    private static final float INPUT_EPSILON = 0.001f;

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val entityManager = world.getEntityManager();
        val delta = world.getOrCreateResource(Time.class).getTimeStepInSeconds();

        entities.forEach(entity -> {
            val input = entityManager.getComponentOf(entity, CharacterInput.class).orElseThrow();
            val stats = entityManager.getComponentOf(entity, MovementStats.class).orElseThrow();
            val velocity = entityManager.getComponentOf(entity, Velocity.class).orElseThrow();

            if (input.move.lengthSquared() > INPUT_EPSILON * INPUT_EPSILON) {
                val maxSpeed = (velocity.velocity.lengthSquared() >= stats.maxSpeed * stats.maxSpeed)
                        ? velocity.velocity.length()
                        : stats.maxSpeed;

                velocity.velocity.add(input.move.normalize(stats.acceleration * delta));
                if (velocity.velocity.lengthSquared() != 0) {
                    val newSpeed = Math.min(velocity.velocity.length(), maxSpeed);
                    velocity.velocity.normalize(newSpeed);
                }
            }
        });
    }
}
