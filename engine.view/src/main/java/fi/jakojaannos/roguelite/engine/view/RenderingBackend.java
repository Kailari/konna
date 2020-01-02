package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.view.rendering.SpriteBatch;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormatBuilder;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderBuilder;
import fi.jakojaannos.roguelite.engine.view.text.TextRenderer;

import java.nio.file.Path;

public interface RenderingBackend {
    Viewport getViewport(Window window);

    Camera getCamera(Viewport viewport);

    // TODO: Make the text renderer use sensible coordinate systems and remove the camera parameter
    //  (Handle on-screen/in-world rendering with camera matrix UBOs instead of manually)
    TextRenderer getTextRenderer(Path assetRoot, Camera camera);

    SpriteBatch createSpriteBatch(Path assetRoot, String shader);

    Mesh createMesh(VertexFormat vertexFormat);

    VertexFormatBuilder createVertexFormat();

    ShaderBuilder createShaderProgram();
}
