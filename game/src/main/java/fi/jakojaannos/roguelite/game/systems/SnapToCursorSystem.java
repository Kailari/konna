package fi.jakojaannos.roguelite.game.systems;

import java.util.stream.Stream;

import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.game.data.components.CrosshairTag;

public class SnapToCursorSystem implements EcsSystem<SnapToCursorSystem.Resources, SnapToCursorSystem.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var cursorPosition = resources.mouse.calculatePositionUnderCursor(resources.cameraProperties);

        entities.map(entity -> entity.getData().transform.position)
                .forEach(entityPosition -> entityPosition.set(cursorPosition));
    }

    public static record Resources(Mouse mouse, CameraProperties cameraProperties) {}

    public static record EntityData(Transform transform, CrosshairTag crosshairTag) {}
}
