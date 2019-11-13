package fi.jakojaannos.roguelite.engine.lwjgl.input;

import fi.jakojaannos.roguelite.engine.input.*;
import fi.jakojaannos.roguelite.engine.lwjgl.view.LWJGLWindow;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

@Slf4j
public class LWJGLInputProvider implements InputProvider {
    private static final double MOUSE_EPSILON = 0.0001;
    private final Queue<InputEvent> inputEvents;

    private int viewportWidth;
    private int viewportHeight;
    private double mouseX, mouseY;
    private boolean justResized;

    public LWJGLInputProvider(LWJGLWindow lwjglWindow, boolean enableForceClose) {
        this.inputEvents = new ArrayDeque<>();

        this.viewportWidth = 1;
        this.viewportHeight = 1;
        lwjglWindow.addResizeCallback((width, height) -> {
            this.viewportWidth = width;
            this.viewportHeight = height;
            this.justResized = true;
        });

        val windowId = lwjglWindow.getId();
        glfwSetKeyCallback(windowId, (window, key, scancode, action, mods) -> {
            ButtonInput.Action inputAction;
            switch (action) {
                case GLFW_RELEASE:
                    inputAction = ButtonInput.Action.RELEASE;
                    break;
                case GLFW_PRESS:
                    inputAction = ButtonInput.Action.PRESS;
                    break;
                case GLFW_REPEAT:
                    inputAction = ButtonInput.Action.REPEAT;
                    break;
                default:
                    LOG.error("Unknown key input action!");
                    return;
            }
            this.inputEvents.offer(
                    new InputEvent(
                            new ButtonInput(
                                    InputButton.Keyboard.get(key)
                                                        .orElse(InputButton.Keyboard.KEY_UNKNOWN),
                                    inputAction)));

            if (enableForceClose && key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                LOG.info("Received force close signal. Sending WindowShouldClose notify.");
                glfwSetWindowShouldClose(windowId, true);
            }
        });

        glfwSetCursorPosCallback(windowId, (window, x, y) -> {
            x = x / this.viewportWidth;
            y = y / this.viewportHeight;

            // In case we just resized, update cached position and skip sending delta-events
            if (this.justResized) {
                this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.X_POS, (float) x)));
                this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.Y_POS, (float) y)));
                this.mouseX = x;
                this.mouseY = y;
                this.justResized = false;
                return;
            }

            val deltaX = this.mouseX - x;
            if (Math.abs(deltaX) > MOUSE_EPSILON) {
                this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.X, (float) deltaX)));
                this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.X_POS, (float) x)));
                this.mouseX = x;
            }

            val deltaY = this.mouseY - y;
            if (Math.abs(deltaY) > MOUSE_EPSILON) {
                this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.Y, (float) deltaY)));
                this.inputEvents.offer(new InputEvent(new AxialInput(InputAxis.Mouse.Y_POS, (float) y)));
                this.mouseY = y;
            }
        });
    }

    @Override
    public Queue<InputEvent> pollEvents() {
        return inputEvents;
    }

    @Override
    public int mapScancode(InputButton.Keyboard key) {
        return glfwGetKeyScancode(key.getKey());
    }
}
