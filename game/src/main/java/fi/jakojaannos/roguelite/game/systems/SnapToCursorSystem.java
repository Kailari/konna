package fi.jakojaannos.roguelite.game.systems;

import org.joml.Vector2d;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.data.resources.Mouse;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.game.data.components.CrosshairTag;

public class SnapToCursorSystem implements ECSSystem {
    private final Vector2d tmpCursorPosition = new Vector2d();

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.PHYSICS_TICK)
                    .requireResource(Mouse.class)
                    .requireResource(CameraProperties.class)
                    .withComponent(Transform.class)
                    .withComponent(CrosshairTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var mouse = world.getOrCreateResource(Mouse.class);
        final var camProps = world.getOrCreateResource(CameraProperties.class);

        final var entityManager = world.getEntityManager();
        mouse.calculateCursorPositionRelativeToCamera(entityManager, camProps, this.tmpCursorPosition);

        entities.map(entity -> entityManager.getComponentOf(entity, Transform.class).orElseThrow().position)
                .forEach(entityPosition -> entityPosition.set(this.tmpCursorPosition));
    }
}
