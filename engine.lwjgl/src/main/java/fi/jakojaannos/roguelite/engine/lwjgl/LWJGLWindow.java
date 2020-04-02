package fi.jakojaannos.roguelite.engine.lwjgl;

import org.lwjgl.glfw.GLFWWindowSizeCallback;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.view.Window;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class LWJGLWindow implements Window {
    private static final Logger LOG = LoggerFactory.getLogger(LWJGLWindow.class);

    private final long id;
    private final List<ResizeCallback> resizeCallbacks = new ArrayList<>();
    private int width;
    private int height;

    public LWJGLWindow(final int width, final int height) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 4);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        this.id = glfwCreateWindow(width, height, "Konna", NULL, NULL);
        if (this.id == NULL) {
            throw new RuntimeException("Failed to create GLFW window");
        }

        GLFWWindowSizeCallback
                .create((window, newWidth, newHeight) ->
                        {
                            this.width = newWidth;
                            this.height = newHeight;
                            this.resizeCallbacks.stream()
                                                .filter(Objects::nonNull)
                                                .forEach(cb -> cb.call(newWidth, newHeight));
                        })
                .set(this.id);

        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer pContentScaleX = stack.mallocFloat(1);
            final FloatBuffer pContentScaleY = stack.mallocFloat(1);
            glfwGetWindowContentScale(this.id, pContentScaleX, pContentScaleY);

            LOG.debug("Window content scale after creation: {}×{}",
                      pContentScaleX.get(0),
                      pContentScaleY.get(0));

            final IntBuffer pWidth = stack.mallocInt(1);
            final IntBuffer pHeight = stack.mallocInt(1);

            glfwGetWindowSize(this.id, pWidth, pHeight);
            this.width = pWidth.get();
            this.height = pHeight.get();
            LOG.debug("Window size after creation: {}×{}", width, height);

            Optional.ofNullable(glfwGetVideoMode(glfwGetPrimaryMonitor()))
                    .ifPresent(videoMode -> glfwSetWindowPos(this.id,
                                                             (videoMode.width() - pWidth.get(0)) / 2,
                                                             (videoMode.height() - pHeight.get(0)) / 2));
        }
    }

    public long getId() {
        return this.id;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void addResizeCallback(final ResizeCallback callback) {
        this.resizeCallbacks.add(callback);
    }

    @Override
    public void close() {
        glfwFreeCallbacks(this.id);
        glfwDestroyWindow(this.id);
        this.resizeCallbacks.clear();

        Optional.ofNullable(glfwSetErrorCallback(null))
                .ifPresent(Callback::free);
    }

    public void enableVSync() {
        glfwSwapInterval(1);
    }

    @Override
    public void show() {
        glfwShowWindow(this.id);
    }
}
