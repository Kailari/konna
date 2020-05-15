package fi.jakojaannos.konna.engine.renderer;

import fi.jakojaannos.konna.engine.adapters.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.renderer.dispatcher.RenderDispatcherBuilderImpl;
import fi.jakojaannos.roguelite.engine.GameState;

public interface RenderDispatcher {
    static Builder builder() {
        return new RenderDispatcherBuilderImpl();
    }

    void dispatch(Renderer renderer, GameState state, long accumulator);

    interface Builder {
        <TResources, TEntityData> Builder withAdapter(EcsRenderAdapter<TResources, TEntityData> adapter);

        RenderDispatcher build();
    }
}
