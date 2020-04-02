package fi.jakojaannos.roguelite.engine.input;

public record ButtonInput(InputButton button, Action action) {
    public static InputEvent pressed(final InputButton button) {
        return InputEvent.button(new ButtonInput(button, Action.PRESS));
    }

    public static InputEvent released(final InputButton button) {
        return InputEvent.button(new ButtonInput(button, Action.RELEASE));
    }

    public static InputEvent event(final InputButton button, final Action action) {
        return InputEvent.button(new ButtonInput(button, action));
    }

    public enum Action {
        PRESS,
        RELEASE,
        REPEAT,
    }
}
