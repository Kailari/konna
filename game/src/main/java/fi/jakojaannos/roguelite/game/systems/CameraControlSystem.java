package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CameraFollowTargetTag;

public class CameraControlSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .withComponent(Transform.class)
                    .withComponent(CameraFollowTargetTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var cameraEntity = world.getOrCreateResource(CameraProperties.class).cameraEntity;
        if (cameraEntity == null) {
            return;
        }

        final var entityManager = world.getEntityManager();
        entityManager.getComponentOf(cameraEntity, Transform.class)
                     .ifPresent(cameraTransform -> {
                         entities.findFirst()
                                 .flatMap(entity -> entityManager.getComponentOf(entity, Transform.class))
                                 .map(targetTransform -> targetTransform.position)
                                 .ifPresent(cameraTransform.position::set);
                     });
    }
}
