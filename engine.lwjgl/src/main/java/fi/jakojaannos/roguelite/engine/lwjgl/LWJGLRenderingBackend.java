package fi.jakojaannos.roguelite.engine.lwjgl;

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
import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;

import java.nio.file.Path;

public class LWJGLRenderingBackend implements RenderingBackend {
    @Override
    public Camera getCamera(final Viewport viewport) {
        return new LWJGLCamera(viewport);
    }

    @Override
    public Viewport createViewport(final Window window) {
        return new LWJGLViewport(window.getWidth(), window.getHeight());
    }

    @Override
    public TextRenderer getTextRenderer(final Path assetRoot, final Camera camera) {
        return new LWJGLTextRenderer(assetRoot, camera, this);
    }

    @Override
    public SpriteBatch createSpriteBatch(final Path assetRoot, final String shader) {
        return new LWJGLSpriteBatch(assetRoot, shader, this);
    }

    @Override
    public VertexFormatBuilder getVertexFormat() {
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
}
