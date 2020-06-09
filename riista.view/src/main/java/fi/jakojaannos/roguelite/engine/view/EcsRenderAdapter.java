package fi.jakojaannos.roguelite.engine.view;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;

public interface EcsRenderAdapter<TResources, TEntityData> {
    VertexFormat getVertexFormat();

    Mesh getMesh();

    Stream<EntityWriter> tick(
            TResources resources,
            Stream<EntityDataHandle<TEntityData>> entities,
            final long accumulator
    );
}
