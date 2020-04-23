package fi.jakojaannos.roguelite.game.view.systems;

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

public final class TurretBaseAdapter implements EcsRenderAdapter<TurretBaseAdapter.Resources, TurretBaseAdapter.EntityData> {
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

    public TurretBaseAdapter(
            final Path assetRoot,
            final RenderingBackend backend,
            final AssetRegistry<Sprite> spriteRegistry,
            final Camera camera
    ) {
        this.camera = camera;
        this.shader = backend.createShaderProgram()
                             .vertexShader(assetRoot.resolve("shaders/entities/turret.vert"))
                             .attributeLocation(0, "in_pos")
                             .attributeLocation(1, "in_uv")
                             .attributeLocation(2, "in_translation")
                             .attributeLocation(3, "in_frame")
                             .fragmentDataLocation(0, "out_frag_color")
                             .fragmentShader(assetRoot.resolve("shaders/entities/turret.frag"))
                             .build();
        this.shader.use();
        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        this.sprite = spriteRegistry.getByAssetName("sprites/turret");

        this.vertexFormat = backend.createVertexFormat(256)
                                   // Vertex position
                                   .withAttribute(FLOAT, 2, false)
                                   // UV coordinates
                                   .withAttribute(FLOAT, 2, false)
                                   // World position
                                   .withInstanceAttribute(FLOAT, 2, false)
                                   // Frame
                                   .withInstanceAttribute(BYTE, 1, false)
                                   .build();

        this.mesh = backend.createMesh(this.vertexFormat);
        this.mesh.setElements(0, 1, 2,
                              0, 2, 3);


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
                                 1.0f / this.sprite.getRows(),
                                 1.0f / this.sprite.getColumns());

        this.camera.useWorldCoordinates();

        return entities.map(entity -> {
            final var transform = entity.getData().transform;
            final var hasTarget = entity.getData().attackAi.getAttackTarget()
                                                           .isPresent();
            // TODO: Handle using animations
            final int frame;
            if (hasTarget) {
                frame = 1 + (int) ((timeManager.getCurrentGameTime() / 5) % 2);
            } else {
                frame = 0;
            }

            return (buffer, offset) -> write(buffer,
                                             offset,
                                             (float) transform.position.x,
                                             (float) transform.position.y,
                                             (byte) frame);
        });
    }

    private static void write(
            final ByteBuffer buffer,
            final int offset,
            final float x,
            final float y,
            final byte frame
    ) {
        buffer.putFloat(offset, x);
        buffer.putFloat(offset + 4, y);
        buffer.put(offset + 8, frame);
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
