package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector3f;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CameraFollowTargetTag;

import static fi.jakojaannos.roguelite.engine.utilities.assertions.world.GameExpect.whenGame;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraControlSystemTest {
    private CameraProperties cameraProperties;

    void initialState(final World world) {
        cameraProperties = new CameraProperties();
        world.registerResource(cameraProperties);
    }

    @Test
    void doesNothingIfFollowTargetDoesNotExist() {
        whenGame().withSystems(new CameraControlSystem())
                  .withState(this::initialState)
                  .withState(world -> cameraProperties.setPosition(new Vector3f(42, 69, 666)))
                  .runsSingleTick()
                  .expect(state -> assertEquals(new Vector3f(42, 69, 666), cameraProperties.getPosition()));
    }

    @Test
    void isRelativelyCloseToTargetAfterFiveSecondsWhenFollowingFromAfar() {
        whenGame().withSystems(new CameraControlSystem())
                  .withState(this::initialState)
                  .withState(world -> {
                      world.createEntity(new Transform(10, 20),
                                         new CameraFollowTargetTag());
                      cameraProperties.setPosition(new Vector3f(42000, 69000, 666));
                  })
                  .runsForSeconds(5)
                  .expect(state -> assertTrue(cameraProperties.getPosition().distance(10, 20, 666) < 24.0));
    }
}
