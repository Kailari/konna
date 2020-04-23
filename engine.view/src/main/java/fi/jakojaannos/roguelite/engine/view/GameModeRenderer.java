package fi.jakojaannos.roguelite.engine.view;

import java.util.Collection;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public final record GameModeRenderer(
        SystemDispatcher renderDispatcher,
        Collection<EcsRenderAdapter<?, ?>>renderAdapters,
        UserInterface userInterface
) implements AutoCloseable {
    @Override
    public void close() throws Exception {
        this.renderDispatcher.close();
    }
}
