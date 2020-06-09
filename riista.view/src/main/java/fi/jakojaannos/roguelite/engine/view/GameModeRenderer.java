package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;

public final record GameModeRenderer(
        @Deprecated SystemDispatcher legacyDispatcher,
        RenderDispatcher renderDispatcher,
        UserInterface userInterface
) implements AutoCloseable {
    @Override
    public void close() throws Exception {
        this.legacyDispatcher.close();
    }
}
