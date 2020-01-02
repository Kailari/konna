package fi.jakojaannos.roguelite.game.test.global;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLAssetManager;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLRenderingBackend;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.state.GameState;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;
import fi.jakojaannos.roguelite.engine.view.Window;
import fi.jakojaannos.roguelite.game.Roguelite;
import fi.jakojaannos.roguelite.game.test.content.TestAssetManager;
import fi.jakojaannos.roguelite.game.test.view.TestRenderingBackend;
import fi.jakojaannos.roguelite.game.test.view.TestWindow;
import fi.jakojaannos.roguelite.game.view.RogueliteGameRenderer;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.lwjgl.opengl.GL;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

/**
 * Singleton game state to be used by step definitions. Note that this effectively prevents the E2E
 * tests from running from parallel in single test runner instance.
 */
public class GlobalState {
    public static Roguelite game;
    public static GameState state;
    public static Events events;
    public static Queue<InputEvent> inputEvents;
    public static GameRenderer gameRenderer;
    public static Window window;

    @Before
    public void before() {
        game = new Roguelite();
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
                                                                    new LWJGLRenderingBackend(),
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
        try {
            gameRenderer.close();
            window.close();

            if (window instanceof LWJGLWindow) {
                glfwTerminate();
            }
        } catch (Exception ignored) { }
    }

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
        game.tick(state, events);
        game.updateTime();

        renderTick();

        inputEvents.clear();
    }

    private static void renderTick() {
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
}
