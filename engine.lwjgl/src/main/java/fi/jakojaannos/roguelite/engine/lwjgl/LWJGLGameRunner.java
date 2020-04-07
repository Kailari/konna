package fi.jakojaannos.roguelite.engine.lwjgl;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameRunner;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.input.InputProvider;
import fi.jakojaannos.roguelite.engine.view.GameRenderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class LWJGLGameRunner extends GameRunner implements AutoCloseable {
    private final LWJGLWindow window;
    @Nullable private final Callback debugCallback;
    private GameRenderer renderer;

    public LWJGLWindow getWindow() {
        return this.window;
    }

    public LWJGLGameRunner(
            final boolean debugModeEnabled,
            final boolean openGLDebugEnabled,
            final int windowWidth,
            final int windowHeight
    ) {
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

        this.debugCallback = debugModeEnabled && openGLDebugEnabled
                ? GLUtil.setupDebugMessageCallback()
                : null;
    }

    public void run(
            final GameMode defaultGameMode,
            final InputProvider inputProvider,
            final GameRenderer renderer
    ) {
        this.renderer = renderer;
        super.run(defaultGameMode, inputProvider);
        this.renderer = null;
    }

    @Override
    protected boolean shouldContinueLoop() {
        return !glfwWindowShouldClose(this.window.getId());
    }

    @Override
    protected void onStateChange(final GameState state) {
    }

    @Override
    protected void onModeChange(final GameMode gameMode) {
        this.renderer.changeGameMode(gameMode);
    }

    @Override
    protected GameState simulateFrame(
            final GameState state,
            final Accumulator accumulator,
            final InputProvider inputProvider
    ) {
        final var newState = super.simulateFrame(state, accumulator, inputProvider);
        presentGameState(newState, accumulator.get());
        return newState;
    }

    public void presentGameState(
            final GameState state,
            final long accumulator
    ) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        this.renderer.render(state, accumulator);

        glfwSwapBuffers(this.window.getId());
        glfwPollEvents();
    }

    @Override
    public void close() {
        if (this.debugCallback != null) {
            this.debugCallback.close();
        }

        this.window.close();
        glfwTerminate();
    }
}
