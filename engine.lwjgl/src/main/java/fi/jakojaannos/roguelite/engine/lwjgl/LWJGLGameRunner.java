package fi.jakojaannos.roguelite.engine.lwjgl;

import fi.jakojaannos.roguelite.engine.Game;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.state.GameState;
import lombok.Getter;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import java.util.function.BiConsumer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class LWJGLGameRunner<TGame extends Game, TInput extends InputProvider>
        extends GameRunner<TGame, TInput> {
    @Getter
    private final LWJGLWindow window;

    public LWJGLGameRunner(int windowWidth, int windowHeight) {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        this.window = new LWJGLWindow(windowWidth == -1 ? 800 : windowWidth,
                                      windowHeight == -1 ? 600 : windowHeight);

        this.window.enableVSync();
        glfwMakeContextCurrent(this.window.getId());
        this.window.show();

        GL.createCapabilities();
        glClearColor(0.25f, 0.6f, 0.4f, 1.0f);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    protected boolean shouldContinueLoop(TGame game) {
        return super.shouldContinueLoop(game) && !glfwWindowShouldClose(this.window.getId());
    }

    @Override
    public void presentGameState(
            GameState state,
            BiConsumer<GameState, Double> renderer,
            double partialTickAlpha
    ) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        super.presentGameState(state, renderer, partialTickAlpha);

        glfwSwapBuffers(this.window.getId());
        glfwPollEvents();
    }

    @Override
    public void close() {
        this.window.close();
        glfwTerminate();
    }
}
