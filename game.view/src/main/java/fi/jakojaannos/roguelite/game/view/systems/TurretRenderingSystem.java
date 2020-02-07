package fi.jakojaannos.roguelite.game.view.systems;

import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.content.AssetRegistry;
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
import fi.jakojaannos.roguelite.engine.view.rendering.sprite.Sprite;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;

public class TurretRenderingSystem implements ECSSystem {
    private static final int MAX_PER_BATCH = 256;

    private final ShaderProgram shader;
    private final ByteBuffer vertexDataBuffer;
    private final Mesh mesh;
    private final int vertexSizeInBytes;
    private final Camera camera;
    private final Sprite sprite;

    public TurretRenderingSystem(
            final Path assetRoot,
            final RenderingBackend backend,
            final AssetRegistry<Sprite> spriteRegistry,
            final Camera camera
    ) {
        this.camera = camera;
        this.shader = backend.createShaderProgram()
                             .vertexShader(assetRoot.resolve("shaders/entities/turret.vert"))
                             .attributeLocation(0, "in_pos")
                             .attributeLocation(1, "in_alert_status")
                             .attributeLocation(2, "in_shooting_status")
                             .attributeLocation(3, "in_base_rotation")
                             .attributeLocation(4, "in_target_pos")
                             .fragmentDataLocation(0, "out_frag_color")
                             .geometryShader(assetRoot.resolve("shaders/entities/turret.geom"))
                             .fragmentShader(assetRoot.resolve("shaders/entities/turret.frag"))
                             .build();
        this.shader.use();
        this.shader.bindUniformBlock("CameraInfo", EngineUniformBufferObjectIndices.CAMERA);

        this.sprite = spriteRegistry.getByAssetName("sprites/turret");

        final var vertexFormat = backend.createVertexFormat()
                                        // turret position
                                        .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                        // alert status
                                        .withAttribute(VertexAttribute.Type.BYTE, 1, false)
                                        // shooting status
                                        .withAttribute(VertexAttribute.Type.BYTE, 1, false)
                                        // base rotation
                                        .withAttribute(VertexAttribute.Type.FLOAT, 1, false)
                                        // target position
                                        .withAttribute(VertexAttribute.Type.FLOAT, 2, false)
                                        .build();
        this.mesh = backend.createMesh(vertexFormat);
        this.vertexSizeInBytes = vertexFormat.getSizeInBytes();
        this.vertexDataBuffer = MemoryUtil.memAlloc(MAX_PER_BATCH * this.vertexSizeInBytes);
        this.mesh.setElements(IntStream.range(0, MAX_PER_BATCH)
                                       .toArray());

        this.mesh.setVertexData(this.vertexDataBuffer);
    }

    @Override
    public void declareRequirements(final RequirementsBuilder requirements) {
        requirements.addToGroup(RenderSystemGroups.ENTITIES)
                    .requireProvidedResource(Time.class)
                    .withComponent(SpriteInfo.class)
                    .withComponent(Transform.class)
                    .withComponent(AttackAbility.class)
                    .withComponent(AttackAI.class);
    }

    @Override
    public void tick(
            final Stream<Entity> entities,
            final World world
    ) {
        final var entityManager = world.getEntityManager();
        final var timeManager = world.getResource(Time.class);

        this.shader.use();
        // FIXME: Just bind the texture and handle the sprite stuff on the GPU using animation
        //  data through an UBO/SSBO or regular uniforms
        this.sprite.getFrame("default", 0.0)
                   .getTexture()
                   .use();

        this.shader.setUniform1i("time",
                                 (int) (timeManager.getCurrentGameTime() % Integer.MAX_VALUE));
        this.shader.setUniform1i("timestep",
                                 (int) (timeManager.getTimeStep()));

        this.camera.useWorldCoordinates();
        this.mesh.setPointSize(5.0f);
        this.mesh.startDrawing();
        var queued = 0;
        for (final var entity : (Iterable<Entity>) entities::iterator) {
            final var spriteInfo = entityManager.getComponentOf(entity, SpriteInfo.class).orElseThrow();
            if (!spriteInfo.spriteName.equals("sprites/turret")) {
                continue;
            }

            if (queued == MAX_PER_BATCH) {
                flush(queued);
                queued = 0;
            }

            final var transform = entityManager.getComponentOf(entity, Transform.class).orElseThrow();
            final var targetPosition = entityManager.getComponentOf(entity, AttackAbility.class)
                                                    .orElseThrow().targetPosition;
            final var attackTarget = entityManager.getComponentOf(entity, AttackAI.class)
                                                  .orElseThrow().getAttackTarget();

            queueVertex(queued,
                        transform.position.x,
                        transform.position.y,
                        transform.rotation,
                        targetPosition.x,
                        targetPosition.y,
                        attackTarget.isPresent(),
                        attackTarget.isPresent());
            ++queued;
        }

        if (queued > 0) {
            flush(queued);
        }
    }

    private void queueVertex(
            final int n,
            final double x,
            final double y,
            final double rotation,
            final double targetX,
            final double targetY,
            final boolean alerted,
            final boolean shooting
    ) {
        final var offset = n * this.vertexSizeInBytes;
        this.vertexDataBuffer.putFloat(offset, (float) x);
        this.vertexDataBuffer.putFloat(offset + 4, (float) y);
        this.vertexDataBuffer.put(offset + 8, (byte) (alerted ? 1 : 0));
        this.vertexDataBuffer.put(offset + 9, (byte) (shooting ? 1 : 0));
        this.vertexDataBuffer.putFloat(offset + 10, (float) rotation);
        this.vertexDataBuffer.putFloat(offset + 14, (float) targetX);
        this.vertexDataBuffer.putFloat(offset + 18, (float) targetY);
    }

    private void flush(final int queued) {
        this.mesh.updateVertexData(this.vertexDataBuffer,
                                   0,
                                   queued);
        this.mesh.drawAsPoints(queued);
    }
}
