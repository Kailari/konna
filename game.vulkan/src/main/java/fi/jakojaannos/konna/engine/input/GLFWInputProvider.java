package fi.jakojaannos.konna.engine.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import fi.jakojaannos.konna.engine.vulkan.window.Window;
import fi.jakojaannos.roguelite.engine.input.InputAxis;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.input.InputProvider;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLFWInputProvider implements InputProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GLFWInputProvider.class);

    private final Queue<InputEvent> inputEvents = new ArrayDeque<>();
    private final Object lock = new Object();

    @Override
    public Object getLock() {
        return this.lock;
    }

    public GLFWInputProvider(final Window window) {
        glfwSetCursorPosCallback(window.getHandle(), (w, x, y) -> {
            // FIXME: Updating the framebuffer size is required only after window resize
            final int framebufferWidth;
            final int framebufferHeight;
            try (final var stack = stackPush()) {
                final var pWidth = stack.mallocInt(1);
                final var pHeight = stack.mallocInt(1);
                glfwGetFramebufferSize(w, pWidth, pHeight);

                framebufferWidth = pWidth.get(0);
                framebufferHeight = pHeight.get(0);
            }

            // Transform cursor position to Normalized Device Coordinate space (NDC)
            final var normalizedX = (x / framebufferWidth) * 2.0 - 1.0;
            final var normalizedY = (y / framebufferHeight) * 2.0 - 1.0;

            synchronized (getLock()) {
                //LOG.debug("normalized: ({}, {})", normalizedX, normalizedY);
                this.inputEvents.offer(InputEvent.axis(InputAxis.Mouse.X_POS, normalizedX));
                this.inputEvents.offer(InputEvent.axis(InputAxis.Mouse.Y_POS, normalizedY));
            }
        });
    }

    @Override
    public Iterable<InputEvent> pollEvents() {
        final Collection<InputEvent> events;
        synchronized (getLock()) {
            events = List.copyOf(this.inputEvents);
            this.inputEvents.clear();
        }

        return events;
    }
}
