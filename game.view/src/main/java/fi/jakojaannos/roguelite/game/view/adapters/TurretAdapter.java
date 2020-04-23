package fi.jakojaannos.roguelite.game.view.adapters;

import org.joml.Matrix4f;
import org.joml.Vector2d;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.annotation.Without;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.Camera;
import fi.jakojaannos.roguelite.engine.view.EcsRenderAdapter;
import fi.jakojaannos.roguelite.engine.view.EntityWriter;
import fi.jakojaannos.roguelite.engine.view.RenderingBackend;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexFormat;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.EngineUniformBufferObjectIndices;
import fi.jakojaannos.roguelite.engine.view.rendering.shader.ShaderProgram;
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.TurretTag;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;

import static fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute.Type.BYTE;
import static fi.jakojaannos.roguelite.engine.view.rendering.mesh.VertexAttribute.Type.FLOAT;
import static org.lwjgl.system.MemoryStack.stackPush;

public final class TurretAdapter implements EcsRenderAdapter<TurretAdapter.Resources, TurretAdapter.EntityData> {
    private static final Matrix4f MODEL = new Matrix4f();

    private final ShaderProgram shader;
    private final Camera camera;
    private final Sprite sprite;
    private final VertexFormat vertexFormat;
    private final Mesh mesh;

    @Override
    public VertexFormat getVertexFormat() {
        return this.vertexFormat;
    }

    @Override
    public Mesh getMesh() {
        return this.mesh;
    }

    public TurretAdapter(
            final Path assetRoot,
            final RenderingBackend backend,
            final AssetRegistry<Sprite> spriteRegistry,
            final Camera camera
    ) {
        this.camera = camera;
        this.shader = backend.createShaderProgram()
                             .vertexShader(assetRoot.resolve("shaders/entities/turret.vert"))
                             .fragmentDataLocation(0, "out_frag_color")
                             .fragmentShader(assetRoot.resolve("shaders/entities/turret.frag"))
                             .build();
        this.shader.use();
        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        this.sprite = spriteRegistry.getByAssetName("sprites/turret");

        this.vertexFormat = backend.createVertexFormat()
                                   // Vertex position
                                   .withAttribute(FLOAT, 2, false)
                                   // UV coordinates
                                   .withAttribute(FLOAT, 2, false)
                                   // World transformations (4x4 matrix)
                                   .withInstanceAttribute(FLOAT, 4, false)
                                   .withInstanceAttribute(FLOAT, 4, false)
                                   .withInstanceAttribute(FLOAT, 4, false)
                                   .withInstanceAttribute(FLOAT, 4, false)
                                   // Frame
                                   .withInstanceAttribute(BYTE, 1, false)
                                   .build();

        this.mesh = backend.createMesh(this.vertexFormat);
        this.mesh.setElements(0, 1, 2, 0, 2, 3);


        try (final var stack = stackPush()) {
            final var buffer = stack.malloc(4 * this.vertexFormat.getSizeInBytes());

            final var size = 1.0f;
            queueVertex(buffer, 0, -size, -size, 0, 0);
            queueVertex(buffer, 1, size, -size, 1, 0);
            queueVertex(buffer, 2, size, size, 1, 1);
            queueVertex(buffer, 3, -size, size, 0, 1);

            this.mesh.setInstanceData(stack.malloc(256 * this.vertexFormat.getInstanceSizeInBytes()));
            this.mesh.setVertexData(buffer);
        }
    }

    private void queueVertex(
            final ByteBuffer buffer,
            final int index,
            final float x,
            final float y,
            final float u,
            final float v
    ) {
        final var offset = index * this.vertexFormat.getSizeInBytes();
        buffer.putFloat(offset, x);
        buffer.putFloat(offset + 4, y);
        buffer.putFloat(offset + (2 * 4), u);
        buffer.putFloat(offset + (3 * 4), v);
    }

    @Override
    public Stream<EntityWriter> tick(
            final Resources resources,
            final Stream<EntityDataHandle<EntityData>> entities,
            final long accumulator
    ) {
        final var timeManager = resources.timeManager;

        this.shader.use();
        this.sprite.getFrame("default", 0.0)
                   .texture()
                   .use();

        this.shader.setUniform2f("frame_size",
                                 1.0f / this.sprite.getColumns(),
                                 1.0f / this.sprite.getRows());
        this.shader.setUniform1i("rows", this.sprite.getRows());
        this.shader.setUniform1i("columns", this.sprite.getColumns());

        this.camera.useWorldCoordinates();

        return entities.flatMap(entity -> {
            final var transform = entity.getData().transform;
            final var hasTarget = entity.getData().attackAi.getAttackTarget()
                                                           .isPresent();
            // TODO: Handle using animations?
            final int frame;
            final float rotation;
            if (hasTarget) {
                final var target = entity.getData().attackAi.getAttackTarget().get().asHandle();
                final var targetPosition = target.getComponent(Transform.class).orElseThrow();
                rotation = (float) targetPosition.position.sub(transform.position, new Vector2d())
                                                          .angle(new Vector2d(0, -1));

                frame = 1 + (int) ((timeManager.getCurrentGameTime() / 5) % 2);
            } else {
                frame = 0;
                rotation = 0.0f;
            }

            return Stream.of((buffer, offset) -> write(buffer, offset,
                                                       (float) transform.position.x,
                                                       (float) transform.position.y,
                                                       0.0f,
                                                       (byte) frame),
                             ((buffer, offset) -> write(buffer, offset,
                                                        (float) transform.position.x,
                                                        (float) transform.position.y,
                                                        -rotation,
                                                        (byte) (frame + 3))));
        });
    }

    private static void write(
            final ByteBuffer buffer,
            final int offset,
            final float x,
            final float y,
            final float rotation,
            final byte frame
    ) {
        MODEL.identity()
             .setTranslation(x, y, 1)
             .rotateZ(rotation);
        MODEL.get(offset, buffer);
        buffer.put(offset + 64, frame);
    }

    public static record EntityData(
            SpriteInfo info,
            Transform transform,
            AttackAbility attack,
            AttackAI attackAi,
            TurretTag turretTag,
            @Without NoDrawTag noDraw
    ) {}

    public static record Resources(
            TimeManager timeManager
    ) {}
}
