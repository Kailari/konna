package fi.jakojaannos.roguelite.game.view.systems.debug;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.RequirementsBuilder;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.lwjgl.UniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.lwjgl.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.game.DebugConfig;
import fi.jakojaannos.roguelite.game.data.components.Collider;
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
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;

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
    private final int uniformModelMatrix;

    private int vao;
    private int vbo;
    private int ebo;

    private final Matrix4f modelMatrix = new Matrix4f();

    public EntityCollisionBoundsRenderingSystem(
            final Path assetRoot,
            final Camera camera
    ) {
        this.camera = camera;
        this.shader = ShaderProgram.builder()
                                   .vertexShader(assetRoot.resolve("shaders/passthrough.vert"))
                                   .fragmentShader(assetRoot.resolve("shaders/bounds.frag"))
                                   .attributeLocation(0, "in_pos")
                                   .fragmentDataLocation(0, "out_fragColor")
                                   .build();

        this.uniformModelMatrix = this.shader.getUniformLocation("model");
        int uniformCameraInfoBlock = glGetUniformBlockIndex(this.shader.getShaderProgram(), "CameraInfo");
        glUniformBlockBinding(this.shader.getShaderProgram(), uniformCameraInfoBlock, UniformBufferObjectIndices.CAMERA);

        this.vao = glGenVertexArrays();
        glBindVertexArray(this.vao);

        val posX = 0.0f;
        val posY = 0.0f;
        val width = 1.0f;
        val height = 1.0f;
        val vertices = new float[]{
                posX, posY,
                posX + width, posY,
                posX + width, posY + height,
                posX, posY + height,
        };
        this.vbo = glGenBuffers();
        this.ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.ebo);
        val indices = new int[]{0, 1, 3, 2,};
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
            final World world
    ) {
        this.shader.use();
        this.camera.useWorldCoordinates();

        glBindVertexArray(this.vao);
        glBindBuffer(GL_ARRAY_BUFFER, this.vbo);
        entities.forEach(
                entity -> {
                    if (world.getEntityManager().hasComponent(entity, NoDrawTag.class) || (!DebugConfig.renderBounds && world.getEntityManager().hasComponent(entity, SpriteInfo.class))) {
                        return;
                    }

                    Transform transform = world.getEntityManager().getComponentOf(entity, Transform.class).orElseThrow();
                    Collider collider = world.getEntityManager().getComponentOf(entity, Collider.class).orElseThrow();
                    this.shader.setUniformMat4x4(this.uniformModelMatrix,
                                                 modelMatrix.identity()
                                                            .translate((float) transform.position.x,
                                                                       (float) transform.position.y, 0.0f));
                    val vertices = collider.getVerticesInLocalSpace(transform);
                    val vertexArray = new float[8];
                    for (int i = 0, j = 0; i < 4; ++i, j += 2) {
                        vertexArray[j] = (float) vertices[i].x;
                        vertexArray[j + 1] = (float) vertices[i].y;
                    }

                    glBufferData(GL_ARRAY_BUFFER, vertexArray, GL_STATIC_DRAW);
                    glEnableVertexAttribArray(0);
                    glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * 2, 0);

                    glDrawElements(GL_LINE_LOOP, 4, GL_UNSIGNED_INT, 0);
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
