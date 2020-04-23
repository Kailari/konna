package fi.jakojaannos.roguelite.game.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.content.AssetManager;
import fi.jakojaannos.roguelite.engine.data.components.Transform;
import fi.jakojaannos.roguelite.engine.data.resources.CameraProperties;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.utilities.TimeManager;
import fi.jakojaannos.roguelite.engine.view.*;
import fi.jakojaannos.roguelite.engine.view.audio.AudioContext;
import fi.jakojaannos.roguelite.engine.view.rendering.mesh.Mesh;
import fi.jakojaannos.roguelite.engine.view.ui.UserInterface;
import fi.jakojaannos.roguelite.game.data.components.NoDrawTag;
import fi.jakojaannos.roguelite.game.data.components.SpriteInfo;
import fi.jakojaannos.roguelite.game.data.components.TurretTag;
import fi.jakojaannos.roguelite.game.data.components.character.AttackAbility;
import fi.jakojaannos.roguelite.game.data.components.character.enemy.AttackAI;
import fi.jakojaannos.roguelite.game.gamemode.GameplayGameMode;
import fi.jakojaannos.roguelite.game.gamemode.MainMenuGameMode;
import fi.jakojaannos.roguelite.game.view.gamemode.GameplayGameModeRenderer;
import fi.jakojaannos.roguelite.game.view.gamemode.MainMenuGameModeRenderer;
import fi.jakojaannos.roguelite.game.view.systems.TurretBaseAdapter;

import static org.lwjgl.system.MemoryUtil.memAlloc;

public class RogueliteGameRenderer implements GameRenderer {
    private static final Logger LOG = LoggerFactory.getLogger(RogueliteGameRenderer.class);

    private static final int MAX_PER_BATCH = 256;

    private final Camera camera;
    private final AudioContext audioContext;

    private final GameModeRendererFactory stateRenderers = new GameModeRendererFactory();
    private final Map<Class<? extends EcsRenderAdapter>, ByteBuffer> adapterBuffers = new HashMap<>();

    @Nullable
    private GameModeRenderer stateRenderer;

    public Camera getCamera() {
        return this.camera;
    }

    public UserInterface getCurrentUserInterface() {
        return Optional.ofNullable(this.stateRenderer)
                       .map(GameModeRenderer::userInterface)
                       .orElseThrow(() -> new IllegalStateException("Cannot fetch UI: No active game mode!"));
    }

    public RogueliteGameRenderer(
            final Events events,
            final TimeManager timeManager,
            final Path assetRoot,
            final Window window,
            final RenderingBackend backend,
            final AssetManager assetManager
    ) {
        LOG.trace("Constructing GameRenderer...");
        this.audioContext = backend.createAudioContext();

        final var viewport = backend.getViewport(window);
        this.camera = backend.createCamera(viewport);
        window.addResizeCallback(viewport::resize);
        window.addResizeCallback(this.camera::resize);
        viewport.resize(window.getWidth(), window.getHeight());
        this.camera.resize(window.getWidth(), window.getHeight());

        this.stateRenderers.register(GameplayGameMode.GAME_MODE_ID,
                                     (mode) -> GameplayGameModeRenderer.create(events,
                                                                               timeManager,
                                                                               assetRoot,
                                                                               this.camera,
                                                                               assetManager,
                                                                               backend,
                                                                               this.audioContext));
        this.stateRenderers.register(MainMenuGameMode.GAME_MODE_ID,
                                     (mode) -> MainMenuGameModeRenderer.create(events,
                                                                               timeManager,
                                                                               assetRoot,
                                                                               this.camera,
                                                                               assetManager,
                                                                               backend));

        LOG.trace("GameRenderer initialization finished.");
    }

    @Override
    public void render(final GameState state, final long accumulator) {
        // Make sure that the camera configuration matches the current state
        this.camera.updateConfigurationFromState(state);

        // Snap camera to active camera
        final var world = state.world();
        Optional.ofNullable(world.fetchResource(CameraProperties.class).cameraEntity)
                .map(Entity::asHandle)
                .flatMap(cameraEntity -> cameraEntity.getComponent(Transform.class))
                .ifPresent(cameraTransform -> this.camera.setPosition(cameraTransform.position.x,
                                                                      cameraTransform.position.y));

        Optional.ofNullable(this.stateRenderer)
                .ifPresent(renderer -> {
                    renderer.renderDispatcher()
                            .tick(state.world(), List.of());
                    renderer.renderAdapters()
                            .forEach(adapter -> {
                                final var vertexSizeInBytes = adapter.getVertexFormat()
                                                                     .getInstanceSizeInBytes();
                                final var buffer = getBufferFor(adapter);
                                final var mesh = adapter.getMesh();
                                mesh.setPointSize(5.0f);
                                mesh.startDrawing();

                                final var nQueued = new QueueCounter();

                                final var entities = state.world()
                                                          .iterateEntities(
                                                                  new Class[]{
                                                                          SpriteInfo.class,
                                                                          Transform.class,
                                                                          AttackAbility.class,
                                                                          AttackAI.class,
                                                                          TurretTag.class,
                                                                          NoDrawTag.class
                                                                  },
                                                                  new boolean[]{
                                                                          false,
                                                                          false,
                                                                          false,
                                                                          false,
                                                                          false,
                                                                          true
                                                                  },
                                                                  new boolean[]{
                                                                          false,
                                                                          false,
                                                                          false,
                                                                          false,
                                                                          false,
                                                                          false
                                                                  },
                                                                  params -> new TurretBaseAdapter.EntityData(
                                                                          (SpriteInfo) params[0],
                                                                          (Transform) params[1],
                                                                          (AttackAbility) params[2],
                                                                          (AttackAI) params[3],
                                                                          (TurretTag) params[4],
                                                                          (NoDrawTag) params[5])
                                                          );
                                final var resources = new TurretBaseAdapter.Resources(world.fetchResource(TimeManager.class));

                                ((EcsRenderAdapter) adapter).tick(resources, (Stream) StreamSupport.stream(entities, false))
                                                            .forEach(writer -> {
                                                                if (nQueued.value == MAX_PER_BATCH) {
                                                                    flush(buffer, mesh, nQueued.value);
                                                                    nQueued.value = 0;
                                                                }

                                                                final var offset = nQueued.value * vertexSizeInBytes;
                                                                ((EntityWriter) writer).write(buffer, offset);
                                                                nQueued.value = nQueued.value + 1;
                                                            });

                                if (nQueued.value > 0) {
                                    flush(buffer, mesh, nQueued.value);
                                }
                            });
                });
    }

    private void flush(final ByteBuffer buffer, final Mesh mesh, final int count) {
        mesh.updateInstanceData(buffer, 0, count);
        mesh.drawInstanced(count, mesh.getIndexCount());
    }

    private ByteBuffer getBufferFor(final EcsRenderAdapter<?, ?> adapter) {
        return this.adapterBuffers.computeIfAbsent(adapter.getClass(),
                                                   ignored -> memAlloc(MAX_PER_BATCH * adapter.getVertexSizeInBytes()));
    }

    @Override
    public void changeGameMode(final GameMode gameMode) {
        if (this.stateRenderer != null) {
            try {
                this.stateRenderer.close();
            } catch (final Exception e) {
                LOG.error("Destroying the old renderer failed: " + e.getMessage());
            }
        }
        this.stateRenderer = this.stateRenderers.get(gameMode);
    }

    @Override
    public void close() throws Exception {
        LOG.debug("Destroying the renderer...");
        if (this.stateRenderer != null) {
            LOG.debug("\t-> Destroying the state renderer...");
            this.stateRenderer.close();
        }
        this.audioContext.close();
        this.camera.close();
    }

    /**
     * Java specification enforces that local references used in lambdas must be effectively final. This limitation
     * prevents using raw ints as counters in loops written using <code>x.forEach(...)</code> -style calls.
     * <p>
     * To overcome this limitation, just wrap the int in an instance which may then be final, so that the reference is
     * final and incrementing the counter is just some interior mutability within an immutable reference.
     */
    private static final class QueueCounter {
        private int value;
    }
}
