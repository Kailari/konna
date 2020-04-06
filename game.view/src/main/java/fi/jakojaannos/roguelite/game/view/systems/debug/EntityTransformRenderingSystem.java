package fi.jakojaannos.roguelite.game.view.systems.debug;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.legacy.World;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.EngineUniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.view.systems.RenderSystemGroups;

// FIXME: Use batching

public class EntityTransformRenderingSystem implements ECSSystem, AutoCloseable {
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

        final var vertexFormat = backend.createVertexFormat()
                                        .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                        .build();
        this.mesh = backend.createMesh(vertexFormat);
        this.mesh.setElements(0);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final var vertexData = stack.malloc(2 * 4);
            vertexData.putFloat(0, 0.0f);
            vertexData.putFloat(4, 0.0f);
            this.mesh.setVertexData(vertexData);
        }
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.DEBUG)
                    .withoutComponent(NoDrawTag.class)
                    .withComponent(Transform.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();

        this.shader.use();
        this.camera.useWorldCoordinates();

        this.mesh.startDrawing();
        this.mesh.setPointSize(5.0f);
        entities.forEach(
                entity -> {
                    final var hasSprite = entityManager.hasComponent(entity, SpriteInfo.class);
                    if (!DebugConfig.renderTransform && hasSprite) {
                        return;
                    }

                    final Transform transform = entityManager.getComponentOf(entity, Transform.class)
                                                             .orElseThrow();
                    this.shader.setUniformMat4x4("model",
                                                 this.modelMatrix.identity()
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
