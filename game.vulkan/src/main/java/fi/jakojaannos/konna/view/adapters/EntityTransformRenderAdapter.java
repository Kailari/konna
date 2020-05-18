package fi.jakojaannos.konna.view.adapters;

import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;

/**
 * Renders any entities with transform as debug transform handles.
 */
public class EntityTransformRenderAdapter implements EcsRenderAdapter<EcsSystem.NoResources, EntityTransformRenderAdapter.EntityData> {
    @Override
    public void draw(
            final Renderer renderer,
            final EcsSystem.NoResources noResources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final long accumulator
    ) {
        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            renderer.debug().drawTransform(transform);
        });
    }

    public static record EntityData(
            Transform transform,
            @Without NoDrawTag noDraw
    ) {}
}
