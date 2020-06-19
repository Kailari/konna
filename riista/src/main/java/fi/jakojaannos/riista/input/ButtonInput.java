package fi.jakojaannos.riista.input;

public record ButtonInput(InputButton button, Action action) {
    public enum Action {
        PRESS,
        RELEASE,
        REPEAT,
    }
}
