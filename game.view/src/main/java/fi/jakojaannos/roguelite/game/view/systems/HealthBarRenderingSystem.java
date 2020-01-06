package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.data.components.Transform;
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
import fi.jakojaannos.roguelite.game.data.components.character.Health;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class HealthBarRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.UI)
                    .withComponent(Health.class)
                    .withComponent(Transform.class);
    }

    private static final int SIZE_IN_BYTES = 4 * 4;
    private static final int MAX_PER_BATCH = 256 / 8;

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
                             .attributeLocation(1, "in_health")
                             .attributeLocation(2, "in_maxHealth")
                             .geometryShader(assetRoot.resolve("shaders/healthbar.geom"))
                             .fragmentShader(assetRoot.resolve("shaders/healthbar.frag"))
                             .fragmentDataLocation(0, "out_fragColor")
                             .build();

        this.shader.use();
        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);


        val vertexFormat = backend.createVertexFormat()
                                  .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                  .withAttribute(VertexAttribute.Type.FLOAT, 1, false)
                                  .withAttribute(VertexAttribute.Type.FLOAT, 1, false)
                                  .build();
        this.mesh = backend.createMesh(vertexFormat);
        this.vertexDataBuffer = MemoryUtil.memAlloc(MAX_PER_BATCH * SIZE_IN_BYTES);
        this.mesh.setElements(IntStream.range(0, MAX_PER_BATCH)
                                       .toArray());

        this.mesh.setVertexData(this.vertexDataBuffer);
    }


    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.camera.useWorldCoordinates();

        this.shader.use();
        val width = 1.5f;
        val height = 0.25f;
        val offsetX = -width / 2.0f;
        val offsetY = 0.85f;
        this.shader.setUniform2f("healthBarSize", width, height);
        this.shader.setUniform2f("healthBarOffset", offsetX, offsetY);

        val entityManager = world.getEntityManager();
        val timeManager = world.getOrCreateResource(Time.class);
        val healthbarDurationInTicks = timeManager.convertToTicks(5.0);

        this.mesh.setPointSize(10.0f);
        this.mesh.startDrawing();
        var count = 0;
        for (val entity : (Iterable<Entity>) entities::iterator) {
            if (count == MAX_PER_BATCH) {
                this.mesh.updateVertexData(this.vertexDataBuffer, 0, count);
                this.mesh.drawAsPoints(count);
                count = 0;
            }

            val transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            val health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            long ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            if (!health.healthBarAlwaysVisible && ticksSinceDamaged >= healthbarDurationInTicks) {
                continue;
            }

            queueVertex(count * SIZE_IN_BYTES,
                        transform.position.x,
                        transform.position.y,
                        health.currentHealth,
                        health.maxHealth);
            ++count;
        }

        if (count > 0) {
            this.mesh.updateVertexData(this.vertexDataBuffer, 0, count);
            this.mesh.drawAsPoints(count);
        }
    }


    private void queueVertex(
            final int offset,
            final double x,
            final double y,
            final double health,
            final double maxHealth
    ) {
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.putFloat(offset + 8, (float) health);
        this.vertexDataBuffer.putFloat(offset + 12, (float) maxHealth);
    }

    @Override
    public void close() throws Exception {
        this.mesh.close();
        this.shader.close();
        MemoryUtil.memFree(this.vertexDataBuffer);
    }
}
