package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.archetypes.PlayerArchetype;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.character.CharacterInput;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.SlimeAI;
import fi.jakojaannos.roguelite.game.data.resources.Players;
import org.joml.Vector2d;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlimeAIControllerSystemTest {

    private static final double EPSILON = 0.001;

    @Test
    void slimeHopsTowardsPlayer() {
        SlimeAIControllerSystem system = new SlimeAIControllerSystem();
        EntityManager entityManager = EntityManager.createNew(256, 32);
        World world = World.createNew(entityManager);

        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.createOrReplaceResource(Time.class, time);

        Transform playerPos = new Transform(10, 10);
        world.getOrCreateResource(Players.class).setLocalPlayer(PlayerArchetype.create(entityManager, playerPos));

        Entity slime = entityManager.createEntity();
        SlimeAI slimeAI = new SlimeAI();
        slimeAI.jumpForce = 5.0;
        slimeAI.chaseRadiusSquared = 100 * 100;
        entityManager.addComponentTo(slime, slimeAI);
        entityManager.addComponentTo(slime, new CharacterInput());
        Transform slimePos = new Transform(3, 6);
        entityManager.addComponentTo(slime, slimePos);
        entityManager.addComponentTo(slime, new MovementStats());
        Physics physics = new Physics();
        physics.mass = 2.5;
        entityManager.addComponentTo(slime, physics);

        entityManager.applyModifications();
        system.tick(Stream.of(slime), world);

        Vector2d expectedDir = new Vector2d(playerPos.position)
                .sub(slimePos.position)
                .normalize(slimeAI.jumpForce / physics.mass);

        assertEquals(expectedDir.x, physics.acceleration.x, EPSILON);
        assertEquals(expectedDir.y, physics.acceleration.y, EPSILON);
    }
}
