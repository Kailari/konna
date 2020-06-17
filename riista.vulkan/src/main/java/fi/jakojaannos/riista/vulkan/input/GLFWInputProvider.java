package fi.jakojaannos.riista.vulkan.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;
import java.util.Queue;

import fi.jakojaannos.riista.vulkan.internal.window.Window;
import fi.jakojaannos.riista.input.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class GLFWInputProvider implements InputProvider {
    private static final Logger LOG = LoggerFactory.getLogger(GLFWInputProvider.class);
    public static final double MOUSE_DELTA_EPSILON = 0.0001;

    private final Queue<InputEvent> inputEvents = new ArrayDeque<>();
    private final Object lock = new Object();

    private double framebufferWidth;
    private double framebufferHeight;
    private double mouseX;
    private double mouseY;

    public Object getLock() {
        return this.lock;
    }

    public GLFWInputProvider(final Window window) {
        glfwSetCursorPosCallback(window.getHandle(), this::onCursorPosition);
        glfwSetMouseButtonCallback(window.getHandle(), this::onMouseButton);
        glfwSetKeyCallback(window.getHandle(), this::onKey);

        try (final var stack = stackPush()) {
            final var pWidth = stack.mallocInt(1);
            final var pHeight = stack.mallocInt(1);
            glfwGetFramebufferSize(window.getHandle(), pWidth, pHeight);

            this.framebufferWidth = pWidth.get(0);
            this.framebufferHeight = pHeight.get(0);
        }

        window.onResize(this::onFramebufferResize);
    }

    private void onKey(final long window, final int key, final int scancode, final int action, final int mods) {
        synchronized (getLock()) {
            this.inputEvents.offer(InputEvent.button(keyOrUnknown(key), resolveAction(action)));
        }
    }

    private void onMouseButton(final long windowId, final int button, final int action, final int mods) {
        synchronized (getLock()) {
            this.inputEvents.offer(InputEvent.button(InputButton.Mouse.button(button),
                                                     resolveAction(action)));
        }
    }

    private void onCursorPosition(final long windowId, final double x, final double y) {
        // Transform cursor position to Normalized Device Coordinate space (NDC)
        final var normalizedX = (x / this.framebufferWidth) * 2.0 - 1.0;
        final var normalizedY = (y / this.framebufferHeight) * 2.0 - 1.0;

        final var deltaX = this.mouseX - normalizedX;
        final var deltaY = this.mouseY - normalizedY;

        synchronized (getLock()) {
            this.inputEvents.offer(InputEvent.axis(InputAxis.Mouse.X_POS, normalizedX));
            this.inputEvents.offer(InputEvent.axis(InputAxis.Mouse.Y_POS, normalizedY));

            if (Math.abs(deltaX) > MOUSE_DELTA_EPSILON) {
                this.inputEvents.offer(InputEvent.axis(InputAxis.Mouse.X, deltaX));
                this.mouseX = normalizedX;
            }
            if (Math.abs(deltaY) > MOUSE_DELTA_EPSILON) {
                this.inputEvents.offer(InputEvent.axis(InputAxis.Mouse.Y, deltaY));
                this.mouseY = normalizedY;
            }
        }
    }

    private void onFramebufferResize(final int width, final int height) {
        this.framebufferWidth = width;
        this.framebufferHeight = height;
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

    private static InputButton.Keyboard keyOrUnknown(final int key) {
        return InputButton.Keyboard.get(key)
                                   .orElse(InputButton.Keyboard.KEY_UNKNOWN);
    }

    private static ButtonInput.Action resolveAction(final int action) {
        return switch (action) {
            case GLFW_RELEASE -> ButtonInput.Action.RELEASE;
            case GLFW_PRESS -> ButtonInput.Action.PRESS;
            case GLFW_REPEAT -> ButtonInput.Action.REPEAT;
            default -> {
                LOG.error("Unknown key input action \"{}\" defaulting to {} (RELEASE)",
                          action, GLFW_RELEASE);
                yield ButtonInput.Action.RELEASE;
            }
        };
    }
}
