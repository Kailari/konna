package fi.jakojaannos.roguelite.engine.view.dispatcher;

import java.util.ArrayList;
import java.util.Collection;

import fi.jakojaannos.roguelite.engine.view.EcsRenderAdapter;
import fi.jakojaannos.roguelite.engine.view.RenderDispatcher;

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
