package fi.jakojaannos.konna.view.adapters.gameplay;

import java.util.stream.Stream;

import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;

/**
 * Renders any entities with transform as debug transform handles.
 */
public class EntityTransformRenderAdapter implements EcsSystem<EntityTransformRenderAdapter.Resources, EntityTransformRenderAdapter.EntityData, EcsSystem.NoEvents> {
    @Override
    public void tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final NoEvents noEvents
    ) {
        final var renderer = resources.renderer;

        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            renderer.debug().drawTransform(transform);
        });
    }

    public static record Resources(Renderer renderer) {}

    public static record EntityData(
            Transform transform,
            @Without NoDrawTag noDraw
    ) {}
}
