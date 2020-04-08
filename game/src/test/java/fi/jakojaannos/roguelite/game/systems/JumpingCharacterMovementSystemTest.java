package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
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

public class JumpingCharacterMovementSystemTest {
    private static final double EPSILON = 0.001;

    @Test
    void slimeHopsTowardsPlayer() {
        final var system = new JumpingCharacterMovementSystem();
        final var world = World.createNew();

        final var time = new SimpleTimeManager(20);
        world.registerResource(TimeManager.class, time);

        final var playerPos = new Transform(10, 10);
        final var players = new Players();
        players.setLocalPlayer(PlayerArchetype.create(world, time, playerPos).asLegacyEntity());
        world.registerResource(Players.class, players);

        final var slime = world.createEntity();
        JumpingMovementAbility split = JumpingMovementAbility.builder().jumpForce(5.0)
                                                             .build();
        slime.addComponent(split);

        final var slimePos = new Transform(3, 6);
        slime.addComponent(slimePos);

        final var input = new MovementInput();
        slime.addComponent(input);

        final var expectedDir = new Vector2d(playerPos.position)
                .sub(slimePos.position)
                .normalize();
        input.move = expectedDir;

        final var physics = Physics.builder()
                                   .mass(2.5)
                                   .build();
        slime.addComponent(physics);

        world.commitEntityModifications();
        system.tick(Stream.of(slime.asLegacyEntity()), world);

        final var expectedAcceleration = expectedDir.normalize(split.jumpForce / physics.mass, new Vector2d());

        assertEquals(expectedAcceleration.x, physics.acceleration.x, EPSILON);
        assertEquals(expectedAcceleration.y, physics.acceleration.y, EPSILON);
    }
}
