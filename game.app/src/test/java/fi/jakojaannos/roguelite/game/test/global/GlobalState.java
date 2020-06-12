package fi.jakojaannos.roguelite.game.test.global;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;
import fi.jakojaannos.roguelite.engine.view.Window;

/**
 * Singleton game state to be used by step definitions. Note that this effectively prevents the E2E tests from running
 * from parallel in single test runner instance.
 */
public class GlobalState {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalState.class);

    public static SimulationInspector simulation;

    //public static RogueliteGameRenderer gameRenderer;
    public static Window window;
    public static TestTimeManager timeManager;

    public static Random random;

    public static void renderTick() {
        // TODO: Figure out some way of doing presentable state copy here (use code from SimulationThread)
    }


    @Before
    public void before() {
        random = new Random(13376969);

        timeManager = new TestTimeManager(20L);

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
