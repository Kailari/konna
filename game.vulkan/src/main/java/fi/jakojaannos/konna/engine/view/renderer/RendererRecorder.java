package fi.jakojaannos.konna.engine.view.renderer;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.DebugRenderer;
import fi.jakojaannos.konna.engine.view.Renderer;

public class RendererRecorder implements Renderer {
    private final DebugRendererRecorder debugRenderer;

    public void setWriteState(final PresentableState state) {
        this.debugRenderer.setWriteState(state);
    }

    public RendererRecorder() {
        this.debugRenderer = new DebugRendererRecorder();
    }

    @Override
    public DebugRenderer debug() {
        return this.debugRenderer;
    }
}
