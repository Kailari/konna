package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;

import java.nio.file.Path;

public interface RenderingBackend {
    Viewport createViewport(Window window);

    Camera getCamera(Viewport viewport);

    TextRenderer getTextRenderer(Path assetRoot, Camera camera);

    SpriteBatch createSpriteBatch(Path assetRoot, String shader);

    Mesh createMesh(VertexFormat vertexFormat);

    VertexFormatBuilder getVertexFormat();

    ShaderBuilder createShaderProgram();
}
