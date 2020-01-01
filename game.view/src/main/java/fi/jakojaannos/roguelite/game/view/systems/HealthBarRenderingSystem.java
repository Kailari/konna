package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.data.resources.Time;
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
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

public class HealthBarRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.tickBefore(UpdateHUDSystem.class)
                    .tickAfter(LevelRenderingSystem.class)
                    .tickAfter(SpriteRenderingSystem.class)
                    .withComponent(Health.class)
                    .withComponent(Transform.class);
    }

    private static final int SIZE_IN_BYTES = (2 + 1) * 4;

    private final Camera camera;
    private final ShaderProgram shader;

    private final ByteBuffer vertexDataBuffer;
    private final Mesh mesh;

    public HealthBarRenderingSystem(
            final Path assetRoot,
            final Camera camera,
            final RenderingBackend backend
    ) {
        this.camera = camera;
        this.shader = backend.createShaderProgram()
                             .vertexShader(assetRoot.resolve("shaders/healthbar.vert"))
                             .attributeLocation(0, "in_pos")
                             .attributeLocation(1, "in_percent")
                             .fragmentShader(assetRoot.resolve("shaders/healthbar.frag"))
                             .fragmentDataLocation(0, "out_fragColor")
                             .build();

        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);


        val vertexFormat = backend.getVertexFormat()
                                  .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                  .withAttribute(VertexAttribute.Type.FLOAT, 1, false)
                                  .build();
        this.mesh = backend.createMesh(vertexFormat);
        this.vertexDataBuffer = MemoryUtil.memAlloc(4 * SIZE_IN_BYTES);
        this.mesh.setElements(0, 1, 2,
                              3, 0, 2);

        val width = 1.5;
        val height = 0.25;
        val offsetX = -width / 2;
        val offsetY = 0.85;
        updateVertex(0, offsetX, offsetY, 0.0);
        updateVertex(SIZE_IN_BYTES, offsetX + width, offsetY, 1.0);
        updateVertex(2 * SIZE_IN_BYTES, offsetX + width, offsetY + height, 1.0);
        updateVertex(3 * SIZE_IN_BYTES, offsetX, offsetY + height, 0.0);
        this.mesh.setVertexData(this.vertexDataBuffer);
    }


    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.shader.use();
        this.camera.useWorldCoordinates();

        val entityManager = world.getEntityManager();
        val timeManager = world.getOrCreateResource(Time.class);
        val healthbarDurationInTicks = timeManager.convertToTicks(5.0);

        this.mesh.startDrawing();
        entities.forEach(entity -> {
            val transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            val health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            long ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            if (!health.healthBarAlwaysVisible && ticksSinceDamaged >= healthbarDurationInTicks) {
                return;
            }

            this.shader.setUniform1f("health", (float) health.asPercentage());

            this.shader.setUniformMat4x4("model",
                                         new Matrix4f().translate((float) transform.position.x,
                                                                  (float) transform.position.y,
                                                                  (float) 0.0)
            );

            this.mesh.draw(6);
        });

    }


    private void updateVertex(
            final int offset,
            final double x,
            final double y,
            final double percent
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) percent);
    }

    @Override
    public void close() throws Exception {
        this.mesh.close();
        this.shader.close();
        MemoryUtil.memFree(this.vertexDataBuffer);
    }
}
