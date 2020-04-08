package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.utilities.SimpleTimeManager;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.CameraFollowTargetTag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CameraControlSystemTest {
    private CameraControlSystem system;
    private World world;
    private Transform cameraTransform;

    @BeforeEach
    void beforeEach() {
        system = new CameraControlSystem();
        world = World.createNew();

        world.registerResource(TimeManager.class, new SimpleTimeManager(20));

        final Entity cameraEntity = world.getEntityManager().createEntity();
        world.getEntityManager().addComponentTo(cameraEntity, cameraTransform = new Transform());

        world.registerResource(CameraProperties.class, new CameraProperties(cameraEntity));
        world.getEntityManager().applyModifications();
    }

    @Test
    void doesNothingIfFollowTargetDoesNotExist() {
        cameraTransform.position.set(42, 69);
        system.tick(Stream.of(), world);

        assertEquals(new Vector2d(42, 69), cameraTransform.position);
    }

    @Test
    void isRelativelyCloseToTargetAfterFiveSecondsWhenFollowingFromAfar() {
        Entity target = world.getEntityManager().createEntity();
        world.getEntityManager().addComponentTo(target, new Transform(10, 20));
        world.getEntityManager().addComponentTo(target, new CameraFollowTargetTag());
        cameraTransform.position.set(42000, 69000);
        world.getEntityManager().applyModifications();

        for (int i = 0; i < 5 / 0.02; ++i) {
            system.tick(Stream.of(target), world);
        }

        assertTrue(cameraTransform.position.distance(10, 20) < 24.0);
    }
}
