package fi.jakojaannos.roguelite.game.view.systems;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.game.data.components.Health;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.Time;
import lombok.val;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.NULL;

public class HealthBarRenderingSystem implements ECSSystem {
    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.tickBefore(RenderHUDSystem.class)
                    .withComponent(Health.class)
                    .withComponent(Transform.class);
    }

    private static final int SIZE_IN_BYTES = (2 + 1) * 4;

    private final LWJGLCamera camera;
    private final ShaderProgram shader;
    private final int uniformModelMatrix;
    private final int uniformViewMatrix;
    private final int uniformProjectionMatrix;
    private final int uniformHealth;

    private final ByteBuffer vertexDataBuffer;
    private final int vao;
    private final int vbo;
    private final int ebo;

    public HealthBarRenderingSystem(final Path assetRoot, final LWJGLCamera camera) {
        this.camera = camera;
        this.shader = ShaderProgram.builder()
                                   .vertexShader(assetRoot.resolve("shaders/healthbar.vert"))
                                   .attributeLocation(0, "in_pos")
                                   .attributeLocation(1, "in_percent")
                                   .fragmentShader(assetRoot.resolve("shaders/healthbar.frag"))
                                   .fragmentDataLocation(0, "out_fragColor")
                                   .build();

        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        this.uniformViewMatrix = this.shader.getUniformLocation("view");
        this.uniformProjectionMatrix = this.shader.getUniformLocation("projection");
        this.uniformHealth = this.shader.getUniformLocation("health");

        this.vertexDataBuffer = MemoryUtil.memAlloc(4 * SIZE_IN_BYTES);
        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        this.ebo = glGenBuffers();
        val indices = new int[]{
                0, 1, 2,
                3, 0, 2,
        };
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        this.vbo = glGenBuffers();

        val width = 1.5;
        val height = 0.25;
        val offsetX = -width / 2;
        val offsetY = 0.85;

        updateVertex(0, offsetX, offsetY, 0.0);
        updateVertex(SIZE_IN_BYTES, offsetX + width, offsetY, 1.0);
        updateVertex(2 * SIZE_IN_BYTES, offsetX + width, offsetY + height, 1.0);
        updateVertex(3 * SIZE_IN_BYTES, offsetX, offsetY + height, 0.0);

        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, this.vertexDataBuffer, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0,
                              2,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              0);

        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1,
                              1,
                              GL_FLOAT,
                              false,
                              SIZE_IN_BYTES,
                              2 * 4);
    }


    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        this.shader.use();
        this.shader.setUniformMat4x4(uniformProjectionMatrix, this.camera.getProjectionMatrix());
        this.shader.setUniformMat4x4(uniformViewMatrix, this.camera.getViewMatrix());

        glBindVertexArray(this.vao);

        val entityManager = world.getEntityManager();
        val timeManager = world.getOrCreateResource(Time.class);
        val healthbarDurationInTicks = timeManager.convertToTicks(5.0);

        entities.forEach(entity -> {
            val transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            val health = entityManager.getComponentOf(entity, Health.class).orElseThrow();

            long ticksSinceDamaged = timeManager.getCurrentGameTime() - health.lastDamageInstanceTimeStamp;
            if (!health.healthBarAlwaysVisible && ticksSinceDamaged >= healthbarDurationInTicks) {
                return;
            }

            this.shader.setUniform1f(uniformHealth, (float) health.asPercentage());

            this.shader.setUniformMat4x4(uniformModelMatrix,
                                         new Matrix4f().translate((float) transform.position.x,
                                                                  (float) transform.position.y,
                                                                  (float) 0.0)
            );


            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, NULL);
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

}
