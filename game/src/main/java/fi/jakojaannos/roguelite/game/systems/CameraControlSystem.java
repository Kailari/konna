package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.CameraFollowTargetTag;

public class CameraControlSystem implements EcsSystem<CameraControlSystem.Resources, CameraControlSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        entities.findFirst()
                .flatMap(entity -> entity.getComponent(Transform.class))
                .map(targetTransform -> targetTransform.position)
                .ifPresent(resources.cameraProperties::setPosition);
    }

    public static record Resources(CameraProperties cameraProperties) {}

    public static record EntityData(Transform transform, CameraFollowTargetTag followTag) {}
}
