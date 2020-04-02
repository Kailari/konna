package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.JumpingMovementAbility;
import fi.jakojaannos.roguelite.game.data.components.character.MovementInput;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import fi.jakojaannos.roguelite.game.systems.characters.movement.JumpingCharacterMovementSystem;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JumpingCharacterMovementSystemTest {
    private static final double EPSILON = 0.001;

    @Test
    void slimeHopsTowardsPlayer() {
        final var system = new JumpingCharacterMovementSystem();
        final var entityManager = EntityManager.createNew(256, 32);
        final var world = World.createNew(entityManager);

        final var time = new Time(new SimpleTimeManager(20));
        world.provideResource(Time.class, time);

        final var playerPos = new Transform(10, 10);
        world.getOrCreateResource(Players.class)
             .setLocalPlayer(PlayerArchetype.create(entityManager, time, playerPos));

        Entity slime = entityManager.createEntity();
        JumpingMovementAbility split = JumpingMovementAbility.builder().jumpForce(5.0)
                                                             .build();
        entityManager.addComponentTo(slime, split);

        final var slimePos = new Transform(3, 6);
        entityManager.addComponentTo(slime, slimePos);

        final var input = new MovementInput();
        entityManager.addComponentTo(slime, input);

        final var expectedDir = new Vector2d(playerPos.position)
                .sub(slimePos.position)
                .normalize();
        input.move = expectedDir;

        Physics physics = Physics.builder()
                                 .mass(2.5)
                                 .build();
        entityManager.addComponentTo(slime, physics);

        entityManager.applyModifications();
        system.tick(Stream.of(slime), world);

        final var expectedAcceleration = expectedDir.normalize(split.jumpForce / physics.mass, new Vector2d());

        assertEquals(expectedAcceleration.x, physics.acceleration.x, EPSILON);
        assertEquals(expectedAcceleration.y, physics.acceleration.y, EPSILON);
    }
}
