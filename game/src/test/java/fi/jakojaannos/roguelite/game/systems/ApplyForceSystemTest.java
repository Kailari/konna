package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Physics;
import fi.jakojaannos.roguelite.game.data.components.Velocity;
import fi.jakojaannos.roguelite.game.systems.physics.ApplyForceSystem;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApplyForceSystemTest {
    private static final double EPSILON = 0.001;

    private Velocity velocity;
    private Physics physics;

    void initialState(final World world) {
        world.createEntity(velocity = new Velocity(),
                           physics = Physics.builder().build());
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
        whenGame().withSystems(new ApplyForceSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(initialVelocityX, initialVelocityY);
                      physics.acceleration.set(accelerationX, accelerationY);
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      assertEquals(expectedVelocityX, velocity.x, EPSILON);
                      assertEquals(expectedVelocityY, velocity.y, EPSILON);
                  });
    }

    @Test
    void entityAtZeroVelocityIsAccelerated() {
        whenGame().withSystems(new ApplyForceSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(0.0, 0.0);
                      physics.acceleration.set(-13.7, 7.3);
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      assertEquals(-13.7, velocity.x, EPSILON);
                      assertEquals(7.3, velocity.y, EPSILON);
                  });
    }

    @Test
    void movingEntityCanBeStoppedWithOppositeFacingForce() {
        whenGame().withSystems(new ApplyForceSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      velocity.set(18.5, -16.0);
                      physics.acceleration.set(-18.5, 16.0);
                  })
                  .runsSingleTick()
                  .expect(state -> {
                      assertEquals(0.0, velocity.x, EPSILON);
                      assertEquals(0.0, velocity.y, EPSILON);
                  });
    }

    @Test
    void accelerationVectorIsResetAfterTicking() {
        whenGame().withSystems(new ApplyForceSystem())
                  .withState(this::initialState)
                  .withState(world -> physics.acceleration.set(100.0, 200.0))
                  .runsSingleTick()
                  .expect(state -> {
                      assertEquals(0.0, physics.acceleration.x);
                      assertEquals(0.0, physics.acceleration.y);
                  });
    }

    @Test
    void heavierObjectGetsSlowerAcceleration() {
        Velocity heavyVelocity = new Velocity();
        whenGame().withSystems(new ApplyForceSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      physics.mass = 1.0; // lighter one

                      final var heavyPhysics = Physics.builder().mass(100).build();
                      world.createEntity(heavyVelocity, heavyPhysics);

                      physics.applyForce(new Vector2d(12.34, 45.67));
                      heavyPhysics.applyForce(new Vector2d(12.34, 45.67));
                  })
                  .runsSingleTick()
                  .expect(state -> assertTrue(velocity.length() > heavyVelocity.length()));
    }
}
