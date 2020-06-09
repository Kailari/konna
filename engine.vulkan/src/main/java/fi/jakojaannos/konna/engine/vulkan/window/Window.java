package fi.jakojaannos.konna.engine.vulkan.window;

import java.util.ArrayList;
import java.util.Collection;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements AutoCloseable {
    private final long handle;
    private final Collection<ResizeCallback> resizeCallbacks = new ArrayList<>();

    public boolean isOpen() {
        return !glfwWindowShouldClose(this.handle);
    }

    public long getHandle() {
        return this.handle;
    }

    public void setShouldClose() {
        glfwSetWindowShouldClose(this.handle, true);
    }

    public Window(final int width, final int height) {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        this.handle = glfwCreateWindow(width, height, "Hello Vulkan", NULL, NULL);

        glfwSetKeyCallback(this.handle, (win, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
                glfwSetWindowShouldClose(win, true);
            }
        });

        glfwSetFramebufferSizeCallback(this.handle, (window, w, h) -> this.resizeCallbacks.forEach(cb -> cb.onResize(w, h)));
    }

    public void onResize(final ResizeCallback callback) {
        this.resizeCallbacks.add(callback);
    }

    public void handleOSEvents() {
        glfwPollEvents();
    }

    public void show() {
        glfwShowWindow(this.handle);
    }

    @Override
    public void close() {
        glfwDestroyWindow(this.handle);
        glfwTerminate();
    }

    public interface ResizeCallback {
        void onResize(int width, int height);
    }
}
