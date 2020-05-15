package fi.jakojaannos.konna.engine.view;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;

public interface EcsRenderAdapter<TResources, TEntityData> {
    void draw(
            Renderer renderer,
            TResources resources,
            Stream<EntityDataHandle<TEntityData>> entities,
            final long accumulator
    );
}