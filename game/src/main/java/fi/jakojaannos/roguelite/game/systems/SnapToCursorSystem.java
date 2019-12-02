package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.Camera;
import fi.jakojaannos.roguelite.game.data.components.CrosshairTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.game.data.resources.Mouse;
import lombok.NonNull;
import lombok.val;
import org.joml.Vector2d;

import java.util.Optional;
import java.util.stream.Stream;

public class SnapToCursorSystem implements ECSSystem {
    @Override
    public void declareRequirements(@NonNull RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.PHYSICS_TICK)
                    .requireResource(Mouse.class)
                    .requireResource(CameraProperties.class)
                    .withComponent(Transform.class)
                    .withComponent(CrosshairTag.class);
    }

    private final Vector2d tmpCamPos = new Vector2d();

    @Override
    public void tick(
            @NonNull Stream<Entity> entities,
            @NonNull World world,
            double delta
    ) {
        val mouse = world.getResource(Mouse.class);
        val camProps = world.getResource(CameraProperties.class);

        val cursorPosition = Optional.ofNullable(camProps.cameraEntity)
                                     .map(e -> world.getEntityManager().getComponentOf(e, Camera.class))
                                     .filter(Optional::isPresent)
                                     .map(Optional::get)
                                     .map(cam -> mouse.calculateCursorPositionRelativeToCamera(cam, camProps, tmpCamPos))
                                     .orElseGet(() -> tmpCamPos.set(0.0, 0.0));

        entities.forEach(entity -> {
            val transform = world.getEntityManager().getComponentOf(entity, Transform.class).get();
            transform.setPosition(cursorPosition.x, cursorPosition.y);
        });
    }
}
