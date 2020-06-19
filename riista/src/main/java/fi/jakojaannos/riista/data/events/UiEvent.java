package fi.jakojaannos.riista.data.events;

public record UiEvent(String element, Type type) {
    public enum Type {
        CLICK
    }
}
