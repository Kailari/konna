package fi.jakojaannos.roguelite.engine.lwjgl.input;

import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.engine.lwjgl.LWJGLWindow;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;

@Slf4j
public class LWJGLInputProvider implements InputProvider {
    private static final double MOUSE_EPSILON = 0.0001;
    private final Queue<InputEvent> inputEvents;

    private int viewportWidth;
    private int viewportHeight;
    private double mouseX, mouseY;
    private boolean justResized;

    public LWJGLInputProvider(final LWJGLWindow window) {
        this.inputEvents = new ArrayDeque<>();

        this.viewportWidth = window.getWidth();
        this.viewportHeight = window.getHeight();
        window.addResizeCallback((width, height) -> {
            this.viewportWidth = width;
            this.viewportHeight = height;
            this.justResized = true;
        });

        glfwSetKeyCallback(window.getId(), this::keyCallback);
        glfwSetMouseButtonCallback(window.getId(), this::mouseButtonCallback);
        glfwSetCursorPosCallback(window.getId(), this::cursorPositionCallback);
    }

    private void cursorPositionCallback(long window, double x, double y) {
        x = x / this.viewportWidth;
        y = y / this.viewportHeight;

        // In case we just resized, update cached position and skip sending delta-events
        if (this.justResized) {
            this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.X_POS, x)));
            this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.Y_POS, y)));
            this.mouseX = x;
            this.mouseY = y;
            this.justResized = false;
            return;
        }

        val deltaX = this.mouseX - x;
        if (Math.abs(deltaX) > MOUSE_EPSILON) {
            this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.X, deltaX)));
            this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.X_POS, x)));
            this.mouseX = x;
        }

        val deltaY = this.mouseY - y;
        if (Math.abs(deltaY) > MOUSE_EPSILON) {
            this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.Y, deltaY)));
            this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.Y_POS, y)));
            this.mouseY = y;
        }
    }

    private void keyCallback(
            final long window,
            final int key,
            final int scancode,
            final int action,
            final int mods
    ) {
        mapAction(action).ifPresent(inputAction -> this.inputEvents.offer(ButtonInput.event(InputButton.Keyboard.get(key)
                                                                                                                .orElse(InputButton.Keyboard.KEY_UNKNOWN),
                                                                                            inputAction)));
    }

    private void mouseButtonCallback(long window, int button, int action, int mods) {
        mapAction(action)
                .ifPresent(inputAction -> this.inputEvents.offer(ButtonInput.event(InputButton.Mouse.button(button),
                                                                                   inputAction)));
    }

    private Optional<ButtonInput.Action> mapAction(int action) {
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
        return inputEvents;
    }
}
