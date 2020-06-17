package fi.jakojaannos.roguelite.game.test.global;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.lwjgl.vulkan.VkExtent2D;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import java.util.Random;

import fi.jakojaannos.konna.view.KonnaGameModeRenderers;
import fi.jakojaannos.riista.GameRenderAdapter;
import fi.jakojaannos.riista.GameRunnerTimeManager;
import fi.jakojaannos.riista.data.resources.CameraProperties;
import fi.jakojaannos.riista.data.resources.Mouse;
import fi.jakojaannos.riista.utilities.TimeManager;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.assets.MusicTrack;
import fi.jakojaannos.riista.view.assets.SoundEffect;
import fi.jakojaannos.riista.view.audio.AudioContext;
import fi.jakojaannos.riista.view.audio.MusicPlayer;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.renderer.GameRenderAdapterBase;
import fi.jakojaannos.riista.vulkan.renderer.game.RendererRecorder;
import fi.jakojaannos.riista.GameState;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;

import static org.mockito.Mockito.mock;

/**
 * Singleton game state to be used by step definitions. Note that this effectively prevents the E2E tests from running
 * from parallel in single test runner instance.
 */
public class GlobalState {
    public static SimulationInspector simulation;
    public static GameRenderAdapter<PresentableState> renderer;

    public static Random random;

    @Before
    public void before() {
        final var assetManager = new TestAssetManager();
        final var audioContext = new AudioContext() {
            @Override
            public Optional<Integer> nextSource(final int priority) {
                return Optional.empty();
            }

            @Override
            public SoundEffect createEffect(final Path assetRoot, final String filename) {
                return mock(SoundEffect.class);
            }

            @Override
            public MusicPlayer createMusicPlayer() {
                return mock(MusicPlayer.class);
            }

            @Override
            public MusicTrack createTrack(final Path path) {
                return mock(MusicTrack.class);
            }

            @Override
            public void close() {
            }
        };

        random = new Random(13376969);

        // FIXME: dummy camera props updater
        // TODO: Actual VulkanRenderAdapter for visualized tests
        renderer = new GameRenderAdapterBase<>(KonnaGameModeRenderers.create(assetManager, new GameRunnerTimeManager(20L), audioContext),
                                               cameraProperties -> {}
        ) {
            private final PresentableState presentableState = new PresentableState();
            private final RendererRecorder recorder = new RendererRecorder();
            private final VkExtent2D extent = VkExtent2D.calloc();

            @Override
            public void writePresentableState(
                    final GameState gameState,
                    final Collection<Object> events
            ) {
                if (hasActiveRenderDispatcher()) {
                    final var cameraProperties = gameState.world().fetchResource(CameraProperties.class);
                    final var mouse = gameState.world().fetchResource(Mouse.class);
                    final var timeManager = gameState.world().fetchResource(TimeManager.class);

                    this.presentableState.clear(timeManager,
                                                mouse.position,
                                                mouse.clicked,
                                                this.extent,
                                                cameraProperties.getPosition(),
                                                cameraProperties.getViewMatrix());

                    gameState.world().replaceResource(Renderer.class, this.recorder);
                    this.recorder.setWriteState(this.presentableState);

                    super.writePresentableState(gameState, events);
                }
            }

            @Override
            public PresentableState fetchPresentableState() {
                return this.presentableState;
            }
        };

        final var shouldVisualizeTests = Optional.ofNullable(System.getenv("VISUALIZE_TESTS"))
                                                 .map(Boolean::valueOf)
                                                 .orElse(false);
        if (shouldVisualizeTests) {
            Path assetRoot = Paths.get("../assets");
            // TODO: Launch renderer thread
        }
    }

    @After
    public void after() {
        // TODO: cleanup
        // TODO: shut down renderer if one is active (?)
        //  - only shutdown after everything has finished? (can be done by moving the renderer to runner class and using JUnit annotations)
        /*if (window instanceof LWJGLWindow) {
            renderTick();
            try {
                Thread.sleep(500L);
            } catch (InterruptedException ignored) {
            }
        }

        try {
            gameRenderer.close();
            window.close();

            if (window instanceof LWJGLWindow) {
                glfwTerminate();
            }
        } catch (Exception ignored) {
        }*/
    }
}
