package fi.jakojaannos.konna.engine.view.renderer;

import fi.jakojaannos.konna.engine.application.PresentableState;
import fi.jakojaannos.konna.engine.view.DebugRenderer;
import fi.jakojaannos.konna.engine.view.MeshRenderer;
import fi.jakojaannos.konna.engine.view.Renderer;
import fi.jakojaannos.konna.engine.view.UiRenderer;
import fi.jakojaannos.konna.engine.view.renderer.debug.DebugRendererRecorder;
import fi.jakojaannos.konna.engine.view.renderer.mesh.MeshRendererRecorder;
import fi.jakojaannos.konna.engine.view.renderer.ui.UiRendererRecorder;

public class RendererRecorder implements Renderer {
    private final DebugRendererRecorder debugRenderer = new DebugRendererRecorder();
    private final MeshRendererRecorder meshRenderer = new MeshRendererRecorder();
    private final UiRendererRecorder uiRenderer = new UiRendererRecorder();

    public void setWriteState(final PresentableState state) {
        this.debugRenderer.setWriteState(state);
        this.meshRenderer.setWriteState(state);
        this.uiRenderer.setWriteState(state);
    }

    @Override
    public DebugRenderer debug() {
        return this.debugRenderer;
    }

    @Override
    public MeshRenderer mesh() {
        return this.meshRenderer;
    }

    @Override
    public UiRenderer ui() {
        return this.uiRenderer;
    }
}
