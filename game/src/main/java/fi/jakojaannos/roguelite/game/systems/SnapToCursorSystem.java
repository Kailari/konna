package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CrosshairTag;
import lombok.val;
import org.joml.Vector2d;

import java.util.stream.Stream;

public class SnapToCursorSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.PHYSICS_TICK)
                    .requireResource(Mouse.class)
                    .requireResource(CameraProperties.class)
                    .withComponent(Transform.class)
                    .withComponent(CrosshairTag.class);
    }

    private final Vector2d tmpCursorPosition = new Vector2d();

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val mouse = world.getOrCreateResource(Mouse.class);
        val camProps = world.getOrCreateResource(CameraProperties.class);

        val entityManager = world.getEntityManager();
        mouse.calculateCursorPositionRelativeToCamera(entityManager, camProps, tmpCursorPosition);

        entities.map(entity -> entityManager.getComponentOf(entity, Transform.class).orElseThrow().position)
                .forEach(entityPosition -> entityPosition.set(tmpCursorPosition));
    }
}
