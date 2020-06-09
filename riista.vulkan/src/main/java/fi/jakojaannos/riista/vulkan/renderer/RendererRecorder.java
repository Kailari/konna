package fi.jakojaannos.riista.vulkan.renderer;

import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.view.DebugRenderer;
import fi.jakojaannos.riista.view.MeshRenderer;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.vulkan.renderer.debug.DebugRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.mesh.MeshRendererRecorder;
import fi.jakojaannos.riista.vulkan.renderer.ui.UiRendererRecorder;
import fi.jakojaannos.riista.view.ui.UiRenderer;

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
