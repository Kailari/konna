package fi.jakojaannos.roguelite.engine.view;

import java.nio.file.Path;

import fi.jakojaannos.roguelite.engine.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.text.TextRenderer;

public interface RenderingBackend extends AutoCloseable {
    TextRenderer getTextRenderer();

    Viewport getViewport(Window window);

    Camera createCamera(Viewport viewport);

    SpriteBatch createSpriteBatch(Path assetRoot, String shader);

    Mesh createMesh(VertexFormat vertexFormat);

    VertexFormatBuilder createVertexFormat();

    VertexFormatBuilder createVertexFormat(int instanceCount);

    ShaderBuilder createShaderProgram();

    AudioContext createAudioContext();
}
