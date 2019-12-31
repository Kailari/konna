package fi.jakojaannos.roguelite.engine.ui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class UIEvent {
    @Getter private final String element;
    @Getter private final Type type;

    public enum Type {
        CLICK,
        HOVER,
        PRESS,
        RELEASE
    }
}
