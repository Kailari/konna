package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.view.dispatcher.RenderDispatcherBuilderImpl;

public interface RenderDispatcher {
    static Builder builder() {
        return new RenderDispatcherBuilderImpl();
    }

    void render(GameState state, long accumulator);

    interface Builder {
        <TResources, TEntityData> Builder withAdapter(EcsRenderAdapter<TResources, TEntityData> adapter);

        RenderDispatcher build();
    }
}
