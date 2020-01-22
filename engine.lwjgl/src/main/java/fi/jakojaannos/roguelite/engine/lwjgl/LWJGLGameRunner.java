package fi.jakojaannos.roguelite.engine.lwjgl;

import fi.jakojaannos.roguelite.engine.Game;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.GameState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

@Slf4j
public class LWJGLGameRunner<TGame extends Game> extends GameRunner<TGame> {
    @Getter
    private final LWJGLWindow window;

    public LWJGLGameRunner(final boolean debugModeEnabled, int windowWidth, int windowHeight) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        this.window = new LWJGLWindow(windowWidth == -1 ? 800 : windowWidth,
                                      windowHeight == -1 ? 600 : windowHeight);

        glfwMakeContextCurrent(this.window.getId());
        this.window.enableVSync();
        this.window.show();

        GL.createCapabilities();
        glClearColor(0.25f, 0.4f, 0.6f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glfwPollEvents();

        if (debugModeEnabled) {
            GLUtil.setupDebugMessageCallback();
        }
    }

    @Override
    protected boolean shouldContinueLoop(TGame game) {
        return super.shouldContinueLoop(game) && !glfwWindowShouldClose(this.window.getId());
    }

    @Override
    public void presentGameState(
            final GameState state,
            final RendererFunction renderer,
            final double partialTickAlpha,
            final Events events
    ) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        super.presentGameState(state, renderer, partialTickAlpha, events);

        glfwSwapBuffers(this.window.getId());
        glfwPollEvents();
    }

    @Override
    public void close() {
        this.window.close();
        glfwTerminate();
    }
}
