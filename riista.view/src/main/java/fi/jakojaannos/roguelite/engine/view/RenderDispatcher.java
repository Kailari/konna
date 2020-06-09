package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.GameState;

public interface RenderDispatcher {
    void render(GameState state, long accumulator);

    interface Builder {
        <TResources, TEntityData> Builder withAdapter(EcsRenderAdapter<TResources, TEntityData> adapter);

        RenderDispatcher build();
    }
}
