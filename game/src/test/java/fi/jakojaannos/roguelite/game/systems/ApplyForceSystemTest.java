package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplyForceSystemTest {

    private static final double EPSILON = 0.001;

    private EntityManager entityManager;
    private World world;
    private ApplyForceSystem system;
    private Entity entity;
    private Velocity velocity;
    private Physics physics;

    @BeforeEach
    void beforeEach() {
        entityManager = EntityManager.createNew(256, 32);
        world = World.createNew(entityManager);
        system = new ApplyForceSystem();

        entity = entityManager.createEntity();
        velocity = new Velocity();
        entityManager.addComponentTo(entity, velocity);
        physics = new Physics();
        entityManager.addComponentTo(entity, physics);

        entityManager.applyModifications();
    }

    @ParameterizedTest
    @CsvSource({
                       "9.0, 4.0, 7.0, 5.0, 16.0, 9.0",
                       "-9.75, -3.75, 11.5, 2.0, 1.75, -1.75",
                       "5.5, -7.5, 0.0, -7.5, 5.5, -15.0"
               })
    void accelerationIsAddedToVelocityCorrectly(
            final double initialVelocityX,
            final double initialVelocityY,
            final double accelerationX,
            final double accelerationY,
            final double expectedVelocityX,
            final double expectedVelocityY
    ) {
        velocity.velocity.set(initialVelocityX, initialVelocityY);
        physics.acceleration.set(accelerationX, accelerationY);

        system.tick(Stream.of(entity), world);

        assertEquals(expectedVelocityX, velocity.velocity.x, EPSILON);
        assertEquals(expectedVelocityY, velocity.velocity.y, EPSILON);
    }

    @Test
    void entityAtZeroVelocityIsAccelerated() {
        velocity.velocity.set(0.0, 0.0);
        physics.acceleration.set(-13.7, 7.3);

        system.tick(Stream.of(entity), world);

        assertEquals(-13.7, velocity.velocity.x, EPSILON);
        assertEquals(7.3, velocity.velocity.y, EPSILON);
    }

    @Test
    void movingEntityCanBeStoppedWithOppositeFacingForce() {
        velocity.velocity.set(18.5, -16.0);
        physics.acceleration.set(-18.5, 16.0);

        system.tick(Stream.of(entity), world);

        assertEquals(0.0, velocity.velocity.x, EPSILON);
        assertEquals(0.0, velocity.velocity.y, EPSILON);
    }

    @Test
    void accelerationVectorIsResetAfterTicking() {
        physics.acceleration.set(100.0, 200.0);

        system.tick(Stream.of(entity), world);

        assertEquals(0.0, physics.acceleration.x);
        assertEquals(0.0, physics.acceleration.y);
    }

    @Test
    void heavierObjectGetsSlowerAcceleration() {
        Entity heavy = entityManager.createEntity();
        Velocity heavyVelocity = new Velocity();
        entityManager.addComponentTo(heavy, heavyVelocity);
        Physics heavyPhysics = new Physics(100);
        entityManager.addComponentTo(heavy, heavyPhysics);
        physics.mass = 1.0; // lighter one

        physics.applyForce(new Vector2d(12.34, 45.67));
        heavyPhysics.applyForce(new Vector2d(12.34, 45.67));

        entityManager.applyModifications();
        system.tick(Stream.of(entity, heavy), world);

        assertTrue(velocity.velocity.length() > heavyVelocity.velocity.length());
    }
}
