package fi.jakojaannos.roguelite.engine.ui;

public record UIEvent(String element, Type type) {
    public enum Type {
        CLICK,
        @Deprecated
        START_HOVER,
        @Deprecated
        PRESS,
        @Deprecated
        RELEASE,
        @Deprecated
        END_HOVER
    }
}
