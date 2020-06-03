package fi.jakojaannos.konna.view.adapters;

import org.joml.Vector2f;

import java.util.stream.Stream;

import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;

/**
 * Renders any entities with transform as debug transform handles.
 */
public class EntityColliderRenderAdapter implements EcsRenderAdapter<EcsSystem.NoResources, EntityColliderRenderAdapter.EntityData> {
    @Override
    public void draw(
            final Renderer renderer,
            final EcsSystem.NoResources noResources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final long accumulator
    ) {
        entities.forEach(entity -> {
            final var transform = entity.getData().transform;
            final var collider = entity.getData().collider;
            renderer.debug().drawBox(transform,
                                     new Vector2f((float) collider.origin.x, (float) collider.origin.y),
                                     new Vector2f((float) collider.width, (float) collider.height));
        });
    }

    public static record EntityData(
            Transform transform,
            Collider collider,
            @Without NoDrawTag noDraw
    ) {}
}
