package fi.jakojaannos.roguelite.engine.ui;

public record UIEvent(String element, Type type) {
    public enum Type {
        CLICK,
        START_HOVER,
        PRESS,
        RELEASE,
        END_HOVER
    }
}
