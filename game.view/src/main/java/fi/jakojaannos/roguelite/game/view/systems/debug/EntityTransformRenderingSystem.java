package fi.jakojaannos.roguelite.game.view.systems.debug;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLCamera;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.ShaderProgram;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.Transform;
import fi.jakojaannos.roguelite.game.view.systems.SpriteRenderingSystem;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.joml.Matrix4f;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

@Slf4j
public class EntityTransformRenderingSystem implements ECSSystem, AutoCloseable {
    @Override
    public void declareRequirements(RequirementsBuilder requirements) {
        requirements.tickAfter(SpriteRenderingSystem.class)
                    .withComponent(Transform.class);
    }

    private final LWJGLCamera camera;
    private final ShaderProgram shader;
    private final int uniformProjectionMatrix;
    private final int uniformViewMatrix;
    private final int uniformModelMatrix;

    private int vao;
    private int vbo;
    private int ebo;

    private final Matrix4f modelMatrix = new Matrix4f();

    public EntityTransformRenderingSystem(
            final Path assetRoot,
            final LWJGLCamera camera
    ) {
        this.camera = camera;
        this.shader = ShaderProgram.builder()
                                   .vertexShader(assetRoot.resolve("shaders/passthrough.vert"))
                                   .attributeLocation(0, "in_pos")
                                   .build();

        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        this.uniformViewMatrix = this.shader.getUniformLocation("view");
        this.uniformProjectionMatrix = this.shader.getUniformLocation("projection");

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        val vertices = new float[]{0.0f, 0.0f};
        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        val indices = new int[]{0};
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 2, 0);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world,
            final double partialTickAlpha
    ) {
        this.shader.use();
        this.shader.setUniformMat4x4(this.uniformProjectionMatrix, this.camera.getProjectionMatrix());
        this.shader.setUniformMat4x4(this.uniformViewMatrix, this.camera.getViewMatrix());

        glBindVertexArray(this.vao);
        glPointSize(5.0f);
        entities.forEach(
                entity -> {
                    if (world.getEntityManager().hasComponent(entity, NoDrawTag.class) || (!DebugConfig.renderTransform && world.getEntityManager().hasComponent(entity, SpriteInfo.class))) {
                        return;
                    }

                    Transform transform = world.getEntityManager().getComponentOf(entity, Transform.class).orElseThrow();
                    this.shader.setUniformMat4x4(this.uniformModelMatrix,
                                                 modelMatrix.identity()
                                                            .translate((float) transform.position.x,
                                                                       (float) transform.position.y, 0.0f)
                    );
                    glDrawElements(GL_POINTS, 1, GL_UNSIGNED_INT, 0);
                }
        );
    }

    @Override
    public void close() {
        glDeleteVertexArrays(this.vao);
        glDeleteBuffers(this.vbo);
        glDeleteBuffers(this.ebo);

        this.shader.close();
    }
}
