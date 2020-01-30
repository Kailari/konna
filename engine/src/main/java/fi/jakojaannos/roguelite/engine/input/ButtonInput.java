package fi.jakojaannos.roguelite.engine.input;

import lombok.Getter;

public class ButtonInput {
    @Getter private final InputButton button;
    @Getter private final Action action;

    public ButtonInput(final InputButton button, final Action action) {
        this.button = button;
        this.action = action;
    }

    public static InputEvent pressed(final InputButton button) {
        return new InputEvent(new ButtonInput(button, Action.PRESS));
    }

    public static InputEvent released(final InputButton button) {
        return new InputEvent(new ButtonInput(button, Action.RELEASE));
    }

    public static InputEvent event(final InputButton button, final Action action) {
        return new InputEvent(new ButtonInput(button, action));
    }

    public enum Action {
        PRESS,
        RELEASE,
        REPEAT,
    }
}
