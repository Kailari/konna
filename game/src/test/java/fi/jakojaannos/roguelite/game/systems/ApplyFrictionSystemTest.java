package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.data.components.character.MovementStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ApplyFrictionSystemTest {

    private static final double EPSILON = 0.01;

    private EntityManager entityManager;
    private World world;
    private ApplyFrictionSystem system;
    private Entity entity;
    private Velocity velocity;
    private MovementStats stats;

    @BeforeEach
    void beforeEach() {
        entityManager = EntityManager.createNew(256, 32);
        world = World.createNew(entityManager);
        system = new ApplyFrictionSystem();

        Time time = mock(Time.class);
        when(time.getTimeStepInSeconds()).thenReturn(0.02);
        world.createOrReplaceResource(Time.class, time);

        velocity = new Velocity();
        stats = new MovementStats(15.0, 1.0, 5.0);
        entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, velocity);
        entityManager.addComponentTo(entity, stats);

        entityManager.applyModifications();
    }

    @ParameterizedTest
    @CsvSource({
                       "9.2, 13.6, 5.0, 6.4, 9.46",
                       "-15.4, -8.2, 3.2, -12.58, -6.7",
                       "0.2, -15.2, 10.8, 0.06, -4.4",
                       "2.0, 1.0, 8.0, 0.0, 0.0",
                       "0.0, 0.0, 1.0, 0.0, 0.0",
                       "0.0, 0.0, 0.0, 0.0, 0.0",
               })
    void frictionSlowsEntity(
            final double startX,
            final double startY,
            final double friction,
            final double expectedX,
            final double expectedY
    ) {
        stats.friction = friction;
        velocity.velocity.set(startX, startY);

        entityManager.applyModifications();
        for (int i = 0; i < 50; i++) {
            system.tick(Stream.of(entity), world);
        }

        assertEquals(expectedX, velocity.velocity.x, EPSILON);
        assertEquals(expectedY, velocity.velocity.y, EPSILON);
    }

    @Test
    void zeroFrictionDoesntSlowEntities() {
        stats.friction = 0;
        velocity.velocity.set(12.34, 34.56);

        entityManager.applyModifications();
        for (int i = 0; i < 10; i++) {
            system.tick(Stream.of(entity), world);
        }

        assertEquals(12.34, velocity.velocity.x);
        assertEquals(34.56, velocity.velocity.y);
    }

    @Test
    void frictionStopsEntityAfterAWhile() {
        velocity.velocity.set(12.34, 34.56);
        stats.friction = 10.0f;

        entityManager.applyModifications();
        for (int i = 0; i < 200; i++) {
            system.tick(Stream.of(entity), world);
        }

        assertEquals(0.0, velocity.velocity.x);
        assertEquals(0.0, velocity.velocity.y);
    }
}
