package fi.jakojaannos.roguelite.game.systems;

import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.game.data.components.CameraFollowTargetTag;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;

import java.util.stream.Stream;

public class CameraControlSystem implements ECSSystem {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.addToGroup(SystemGroups.LATE_TICK)
                    .withComponent(Transform.class)
                    .withComponent(CameraFollowTargetTag.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        val cameraEntity = world.getOrCreateResource(CameraProperties.class).cameraEntity;
        if (cameraEntity == null) {
            return;
        }

        val entityManager = world.getEntityManager();
        val cameraTransform = entityManager.getComponentOf(cameraEntity, Transform.class).orElseThrow();
        entities.findFirst()
                .flatMap(entity -> entityManager.getComponentOf(entity, Transform.class))
                .map(targetTransform -> targetTransform.position)
                .ifPresent(cameraTransform.position::set);
    }
}
