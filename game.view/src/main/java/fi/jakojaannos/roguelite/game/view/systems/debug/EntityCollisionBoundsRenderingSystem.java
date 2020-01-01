package fi.jakojaannos.roguelite.game.view.systems.debug;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.EngineUniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.components.Collider;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.view.systems.SpriteRenderingSystem;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.file.Path;
import java.util.stream.Stream;

@Slf4j
public class EntityCollisionBoundsRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class)
                    .withComponent(Transform.class)
                    .withComponent(Collider.class);
    }

    private final Camera camera;
    private final ShaderProgram shader;
    private final Mesh mesh;

    private final Matrix4f modelMatrix = new Matrix4f();

    public EntityCollisionBoundsRenderingSystem(
            final Path assetRoot,
            final Camera camera,
            final RenderingBackend backend
    ) {
        this.camera = camera;
        this.shader = backend.createShaderProgram()
                             .vertexShader(assetRoot.resolve("shaders/passthrough.vert"))
                             .fragmentShader(assetRoot.resolve("shaders/bounds.frag"))
                             .attributeLocation(0, "in_pos")
                             .fragmentDataLocation(0, "out_fragColor")
                             .build();

        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        val vertexFormat = backend.getVertexFormat()
                                  .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                  .build();
        this.mesh = backend.createMesh(vertexFormat);
        val posX = 0.0f;
        val posY = 0.0f;
        val width = 1.0f;
        val height = 1.0f;
        try (val stack = MemoryStack.stackPush()) {
            val vertexData = stack.malloc(8 * 4);
            vertexData.putFloat(0, posX);
            vertexData.putFloat(4, posY);

            vertexData.putFloat(8, posX + width);
            vertexData.putFloat(12, posY);

            vertexData.putFloat(16, posX + width);
            vertexData.putFloat(20, posY + height);

            vertexData.putFloat(24, posX);
            vertexData.putFloat(28, posY + height);

            this.mesh.setVertexData(vertexData);
        }
        this.mesh.setElements(0, 1, 3, 2);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.shader.use();
        this.camera.useWorldCoordinates();

        this.mesh.startDrawing();
        entities.forEach(
                entity -> {
                    if (world.getEntityManager().hasComponent(entity, NoDrawTag.class) || (!DebugConfig.renderBounds && world.getEntityManager().hasComponent(entity, SpriteInfo.class))) {
                        return;
                    }

                    Transform transform = world.getEntityManager().getComponentOf(entity, Transform.class).orElseThrow();
                    Collider collider = world.getEntityManager().getComponentOf(entity, Collider.class).orElseThrow();
                    this.shader.setUniformMat4x4("model",
                                                 modelMatrix.identity()
                                                            .translate((float) transform.position.x,
                                                                       (float) transform.position.y, 0.0f));
                    val vertices = collider.getVerticesInLocalSpace(transform);
                    try (val stack = MemoryStack.stackPush()) {
                        val vertexData = stack.malloc(8 * 4);
                        for (int i = 0, j = 0; i < 4; ++i, j += 2) {
                            vertexData.putFloat(j * 4, (float) vertices[i].x);
                            vertexData.putFloat((j + 1) * 4, (float) vertices[i].y);
                        }
                        this.mesh.updateVertexData(vertexData, 0, 4);
                    }

                    this.mesh.drawAsLineLoop(4);
                }
        );
    }

    @Override
    public void close() throws Exception {
        this.mesh.close();
        this.shader.close();
    }
}
