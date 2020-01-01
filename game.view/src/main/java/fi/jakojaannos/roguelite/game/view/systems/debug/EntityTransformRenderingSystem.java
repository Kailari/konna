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
public class EntityTransformRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class)
                    .withComponent(Transform.class);
    }

    private final Camera camera;
    private final ShaderProgram shader;

    private final Mesh mesh;

    private final Matrix4f modelMatrix = new Matrix4f();

    public EntityTransformRenderingSystem(
            final Path assetRoot,
            final Camera camera,
            final RenderingBackend backend
    ) {
        this.camera = camera;
        this.shader = backend.createShaderProgram()
                             .vertexShader(assetRoot.resolve("shaders/passthrough.vert"))
                             .attributeLocation(0, "in_pos")
                             .build();

        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        val vertexFormat = backend.getVertexFormat()
                                  .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                  .build();
        this.mesh = backend.createMesh(vertexFormat);
        this.mesh.setElements(0);
        try (val stack = MemoryStack.stackPush()) {
            val vertexData = stack.malloc(2 * 4);
            vertexData.putFloat(0, 0.0f);
            vertexData.putFloat(4, 0.0f);
            this.mesh.setVertexData(vertexData);
        }
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.shader.use();
        this.camera.useWorldCoordinates();

        this.mesh.startDrawing();
        this.mesh.setPointSize(5.0f);
        entities.forEach(
                entity -> {
                    if (world.getEntityManager().hasComponent(entity, NoDrawTag.class) || (!DebugConfig.renderTransform && world.getEntityManager().hasComponent(entity, SpriteInfo.class))) {
                        return;
                    }

                    Transform transform = world.getEntityManager().getComponentOf(entity, Transform.class).orElseThrow();
                    this.shader.setUniformMat4x4("model",
                                                 modelMatrix.identity()
                                                            .translate((float) transform.position.x,
                                                                       (float) transform.position.y, 0.0f)
                    );
                    this.mesh.drawAsPoints(1);
                }
        );
    }

    @Override
    public void close() throws Exception {
        this.mesh.close();
        this.shader.close();
    }
}
