package fi.jakojaannos.konna.engine.renderer.dispatcher;

import java.util.ArrayList;
import java.util.Collection;

import fi.jakojaannos.konna.engine.view.EcsRenderAdapter;
import fi.jakojaannos.konna.engine.renderer.RenderDispatcher;

public class RenderDispatcherBuilderImpl implements RenderDispatcher.Builder {
    private final Collection<EcsRenderAdapter<?, ?>> adapters = new ArrayList<>();

    @Override
    public <TResources, TEntityData> RenderDispatcher.Builder withAdapter(final EcsRenderAdapter<TResources, TEntityData> adapter) {
        this.adapters.add(adapter);
        return this;
    }

    @Override
    public RenderDispatcher build() {
        return new RenderDispatcherImpl(this.adapters);
    }
}
