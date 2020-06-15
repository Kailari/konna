package fi.jakojaannos.roguelite.game.test.global;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.joml.Vector2f;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Random;

import fi.jakojaannos.konna.view.KonnaGameModeRenderers;
import fi.jakojaannos.riista.GameRenderAdapter;
import fi.jakojaannos.riista.data.components.Transform;
import fi.jakojaannos.riista.data.events.UiEvent;
import fi.jakojaannos.riista.view.DebugRenderer;
import fi.jakojaannos.riista.view.MeshRenderer;
import fi.jakojaannos.riista.view.Renderer;
import fi.jakojaannos.riista.view.assets.MusicTrack;
import fi.jakojaannos.riista.view.assets.SkeletalMesh;
import fi.jakojaannos.riista.view.assets.SoundEffect;
import fi.jakojaannos.riista.view.audio.AudioContext;
import fi.jakojaannos.riista.view.audio.MusicPlayer;
import fi.jakojaannos.riista.view.ui.UiElement;
import fi.jakojaannos.riista.view.ui.UiRenderer;
import fi.jakojaannos.riista.vulkan.application.PresentableState;
import fi.jakojaannos.riista.vulkan.renderer.GameRenderAdapterBase;
import fi.jakojaannos.riista.vulkan.renderer.game.RendererRecorder;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;

import static org.mockito.Mockito.mock;

/**
 * Singleton game state to be used by step definitions. Note that this effectively prevents the E2E tests from running
 * from parallel in single test runner instance.
 */
public class GlobalState {
    public static SimulationInspector simulation;

    //public static RogueliteGameRenderer gameRenderer;
    public static TestTimeManager timeManager;
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

        timeManager = new TestTimeManager(20L);

        // FIXME: dummy camera props updater
        // TODO: Actual VulkanRenderAdapter for visualized tests
        renderer = new GameRenderAdapterBase<>(KonnaGameModeRenderers.create(assetManager, timeManager, audioContext),
                                               cameraProperties -> {}
        ) {
            @Override
            public void writePresentableState(
                    final GameState gameState,
                    final Collection<Object> events
            ) {
                if (this.hasActiveRenderDispatcher()) {
                    gameState.world().replaceResource(Renderer.class, new RendererRecorder(
                            new DebugRenderer() {
                                @Override
                                public void drawTransform(final Transform transform) {
                                }

                                @Override
                                public void drawBox(
                                        final Transform transform, final Vector2f offset, final Vector2f size
                                ) {
                                }
                            },
                            new MeshRenderer() {
                                @Override
                                public void drawSkeletal(
                                        final Transform transform,
                                        final SkeletalMesh mesh,
                                        final String animation,
                                        final int frame
                                ) {
                                }
                            },
                            new UiRenderer() {
                                @Override
                                public void setValue(final String key, final Object value) {
                                }

                                @Override
                                public Collection<UiEvent> draw(final UiElement element) {
                                    return Collections.emptyList();
                                }
                            }
                    ));

                    super.writePresentableState(gameState, events);
                }
            }

            @Override
            public PresentableState fetchPresentableState() {
                return new PresentableState();
            }
        };

        // TODO: Create render adapters for UI/render testing (?)

        final var shouldVisualizeTests = Optional.ofNullable(System.getenv("VISUALIZE_TESTS"))
                                                 .map(Boolean::valueOf)
                                                 .orElse(false);
        if (shouldVisualizeTests) {
            Path assetRoot = Paths.get("../assets");
            // TODO: Launch renderer thread
        }
        /*
        gameRenderer = Optional.ofNullable(System.getenv("VISUALIZE_TESTS"))
                               .map(Boolean::valueOf)
                               .filter(Boolean::booleanValue)
                               .map(ignored -> {
                                   glfwInit();
                                   LWJGLWindow lwjglWindow = new LWJGLWindow(800, 600);
                                   window = lwjglWindow;
                                   lwjglWindow.show();
                                   glfwMakeContextCurrent(lwjglWindow.getId());
                                   GL.createCapabilities();
                                   glfwSwapInterval(0);
                                   return new RogueliteGameRenderer(gameRunner.getEvents(),
                                                                    gameRunner.getTimeManager(),
                                                                    assetRoot,
                                                                    window,
                                                                    new LWJGLRenderingBackend(assetRoot),
                                                                    new LWJGLAssetManager(assetRoot));
                               })
                               .orElseGet(() -> new RogueliteGameRenderer(gameRunner.getEvents(),
                                                                          gameRunner.getTimeManager(),
                                                                          assetRoot,
                                                                          window = new TestWindow(800, 600),
                                                                          new TestRenderingBackend(),
                                                                          new TestAssetManager(assetRoot)));

         */
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
