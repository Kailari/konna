package fi.jakojaannos.roguelite.engine.lwjgl.input;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLWindow;
import fi.jakojaannos.roguelite.engine.view.Window;

import static org.lwjgl.glfw.GLFW.*;

public class LWJGLInputProvider implements InputProvider {
    private static final Logger LOG = LoggerFactory.getLogger(LWJGLInputProvider.class);

    private static final double MOUSE_EPSILON = 0.0001;
    private final Queue<InputEvent> inputEvents;

    private int viewportWidth;
    private int viewportHeight;
    private double mouseX;
    private double mouseY;
    private boolean justResized;

    private final Object lock = new Object();

    @Override
    public Object getLock() {
        return this.lock;
    }

    public LWJGLInputProvider(final LWJGLWindow window) {
        this(window.getId(), window.getWidth(), window.getHeight(), window::addResizeCallback);
    }

    public LWJGLInputProvider(
            final long window,
            final int viewportWidth,
            final int viewportHeight,
            final Consumer<Window.ResizeCallback> resizeCallbackConsumer
    ) {
        this.inputEvents = new ArrayDeque<>();

        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        resizeCallbackConsumer.accept((width, height) -> {
            this.viewportWidth = width;
            this.viewportHeight = height;
            this.justResized = true;
        });

        glfwSetKeyCallback(window, this::keyCallback);
        glfwSetMouseButtonCallback(window, this::mouseButtonCallback);
        glfwSetCursorPosCallback(window, this::cursorPositionCallback);
    }

    private void cursorPositionCallback(final long window, final double windowX, final double windowY) {
        synchronized (this.getLock()) {
            final var x = windowX / this.viewportWidth;
            final var y = windowY / this.viewportHeight;

            // In case we just resized, update cached position and skip sending delta-events
            if (this.justResized) {
                this.inputEvents.offer(InputEvent.axial(new AxialInput(InputAxis.Mouse.X_POS, x)));
                this.inputEvents.offer(InputEvent.axial(new AxialInput(InputAxis.Mouse.Y_POS, y)));
                this.mouseX = x;
                this.mouseY = y;
                this.justResized = false;
                return;
            }

            final var deltaX = this.mouseX - x;
            if (Math.abs(deltaX) > MOUSE_EPSILON) {
                this.inputEvents.offer(InputEvent.axial(new AxialInput(InputAxis.Mouse.X, deltaX)));
                this.inputEvents.offer(InputEvent.axial(new AxialInput(InputAxis.Mouse.X_POS, x)));
                this.mouseX = x;
            }

            final var deltaY = this.mouseY - y;
            if (Math.abs(deltaY) > MOUSE_EPSILON) {
                this.inputEvents.offer(InputEvent.axial(new AxialInput(InputAxis.Mouse.Y, deltaY)));
                this.inputEvents.offer(InputEvent.axial(new AxialInput(InputAxis.Mouse.Y_POS, y)));
                this.mouseY = y;
            }
        }
    }

    private void keyCallback(
            final long window,
            final int key,
            final int scancode,
            final int action,
            final int mods
    ) {
        synchronized (this.getLock()) {
            mapAction(action).ifPresent(
                    inputAction -> this.inputEvents.offer(ButtonInput.event(keyOrUnknown(key), inputAction)));
        }
    }

    private InputButton.Keyboard keyOrUnknown(final int key) {
        return InputButton.Keyboard.get(key)
                                   .orElse(InputButton.Keyboard.KEY_UNKNOWN);
    }

    private void mouseButtonCallback(final long window, final int button, final int action, final int mods) {
        synchronized (this.getLock()) {
            mapAction(action)
                    .ifPresent(inputAction -> this.inputEvents.offer(ButtonInput.event(InputButton.Mouse.button(button),
                                                                                       inputAction)));
        }
    }

    private Optional<ButtonInput.Action> mapAction(final int action) {
        switch (action) {
            case GLFW_RELEASE:
                return Optional.of(ButtonInput.Action.RELEASE);
            case GLFW_PRESS:
                return Optional.of(ButtonInput.Action.PRESS);
            case GLFW_REPEAT:
                return Optional.of(ButtonInput.Action.REPEAT);
            default:
                LOG.error("Unknown key input action: {}", action);
                return Optional.empty();
        }
    }

    @Override
    public Queue<InputEvent> pollEvents() {
        return this.inputEvents;
    }
}
