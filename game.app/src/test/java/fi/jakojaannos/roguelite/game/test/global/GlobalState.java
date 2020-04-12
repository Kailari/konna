package fi.jakojaannos.roguelite.game.test.global;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLAssetManager;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLRenderingBackend;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.game.test.content.TestAssetManager;
import fi.jakojaannos.roguelite.game.test.view.TestRenderingBackend;
import fi.jakojaannos.roguelite.game.test.view.TestWindow;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Singleton game state to be used by step definitions. Note that this effectively prevents the E2E tests from running
 * from parallel in single test runner instance.
 */
public class GlobalState {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalState.class);

    public static GameRunner gameRunner;
    public static GameMode mode;
    public static GameState state;
    public static Events events;
    public static Queue<InputEvent> inputEvents;
    public static RogueliteGameRenderer gameRenderer;
    public static Window window;
    public static TestTimeManager timeManager;

    public static Random random;

    public static <T> Optional<T> getComponentOf(
            Entity player,
            Class<T> componentClass
    ) {
        return state.world()
                    .getEntityManager()
                    .getComponentOf(player, componentClass);
    }

    public static void renderTick() {
        if (window instanceof LWJGLWindow) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        gameRenderer.render(state, 0);

        if (window instanceof LWJGLWindow) {
            glfwSwapBuffers(((LWJGLWindow) window).getId());
            glfwPollEvents();
        }
    }

    public static void simulateTick() {
        final var accumulator = new GameRunner.Accumulator();
        accumulator.accumulate(timeManager.getTimeStep());
        state = gameRunner.simulateFrame(state, accumulator, () -> inputEvents);
        inputEvents.clear();
    }

    public static void simulateSeconds(double seconds) {
        final var accumulator = new GameRunner.Accumulator();
        final var ticks = timeManager.convertToTicks(seconds);
        accumulator.accumulate(ticks * timeManager.getTimeStep());
        state = gameRunner.simulateFrame(state, accumulator, () -> inputEvents);
        inputEvents.clear();
    }

    @Before
    public void before() {
        random = new Random(13376969);

        timeManager = new TestTimeManager(20L);
        gameRunner = new GameRunner(timeManager) {
            @Override
            protected boolean shouldContinueLoop() {
                return false;
            }

            @Override
            protected void onStateChange(final GameState state) {
            }

            @Override
            protected void onModeChange(final GameMode gameMode) {
                gameRenderer.changeGameMode(gameMode);
                mode = gameMode;
            }
        };
        Path assetRoot = Paths.get("../assets");
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
        events = gameRunner.getEvents();
        inputEvents = new ArrayDeque<>();
    }

    @After
    public void after() {
        if (window instanceof LWJGLWindow) {
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
        }
    }
}
