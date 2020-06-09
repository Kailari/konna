package fi.jakojaannos.roguelite.engine.lwjgl;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.lwjgl.audio.LWJGLAudioContext;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLViewport;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.LWJGLSpriteBatch;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh.LWJGLMesh;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.mesh.LWJGLVertexFormatBuilder;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader.LWJGLShaderBuilder;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.text.LWJGLTextRenderer;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.Viewport;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.riista.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;

public class LWJGLRenderingBackend implements RenderingBackend {
    private final LWJGLTextRenderer textRenderer;

    @Override
    public TextRenderer getTextRenderer() {
        return this.textRenderer;
    }

    public LWJGLRenderingBackend(final Path assetRoot) {
        this.textRenderer = new LWJGLTextRenderer(assetRoot, this);
    }

    @Override
    public Viewport getViewport(final Window window) {
        return new LWJGLViewport(window.getWidth(), window.getHeight());
    }

    @Override
    public Camera createCamera(final Viewport viewport) {
        return new LWJGLCamera(viewport);
    }

    @Override
    public SpriteBatch createSpriteBatch(final Path assetRoot, final String shader) {
        return new LWJGLSpriteBatch(assetRoot, shader, this);
    }

    @Override
    public VertexFormatBuilder createVertexFormat() {
        return new LWJGLVertexFormatBuilder();
    }

    @Override
    public Mesh createMesh(final VertexFormat vertexFormat) {
        return new LWJGLMesh(vertexFormat);
    }

    @Override
    public ShaderBuilder createShaderProgram() {
        return new LWJGLShaderBuilder();
    }

    @Override
    public AudioContext createAudioContext() {
        return new LWJGLAudioContext(16);
    }

    @Override
    public void close() throws Exception {
        this.textRenderer.close();
    }
}
