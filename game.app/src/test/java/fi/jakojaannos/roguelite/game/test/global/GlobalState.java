package fi.jakojaannos.roguelite.game.test.global;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.lwjgl.opengl.GL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.Random;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLAssetManager;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLRenderingBackend;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.game.RogueliteGame;
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
    public static RogueliteGame game;
    public static GameState state;
    public static Events events;
    public static Queue<InputEvent> inputEvents;
    public static RogueliteGameRenderer gameRenderer;
    public static Window window;
    public static TestTimeManager timeManager;

    public static Random random;

    public static <T extends Component> Optional<T> getComponentOf(
            Entity player,
            Class<T> componentClass
    ) {
        return state.getWorld()
                    .getEntityManager()
                    .getComponentOf(player, componentClass);
    }

    public static void simulateTick() {
        inputEvents.forEach(events.getInput()::fire);
        state = game.tick(state, events);
        game.updateTime();

        inputEvents.clear();
    }

    public static void renderTick() {
        if (window instanceof LWJGLWindow) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        }

        gameRenderer.render(state, 0.0, events);

        if (window instanceof LWJGLWindow) {
            glfwSwapBuffers(((LWJGLWindow) window).getId());
            glfwPollEvents();
        }
    }

    public static void simulateSeconds(double seconds) {
        int ticks = (int) (seconds / 0.02);
        for (int i = 0; i < ticks; ++i) {
            simulateTick();
        }
    }

    @Before
    public void before() {
        random = new Random(13376969);

        timeManager = new TestTimeManager(20L);
        game = new RogueliteGame(timeManager);
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
                                   return new RogueliteGameRenderer(assetRoot,
                                                                    window,
                                                                    new LWJGLRenderingBackend(assetRoot),
                                                                    new LWJGLAssetManager(assetRoot));
                               })
                               .orElseGet(() -> new RogueliteGameRenderer(assetRoot,
                                                                          window = new TestWindow(800, 600),
                                                                          new TestRenderingBackend(),
                                                                          new TestAssetManager(assetRoot)));
        events = new Events();
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
        } catch (Exception ignored) { }
    }
}
