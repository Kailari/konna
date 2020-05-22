package fi.jakojaannos.konna.engine.view.ui;

import java.util.Iterator;
import java.util.List;

public enum Sides implements Iterable<Sides> {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT,
    HORIZONTAL,
    VERTICAL,
    ALL;

    @Override
    public Iterator<Sides> iterator() {
        final var actualSides = switch (this) {
            case ALL -> List.of(TOP, BOTTOM, LEFT, RIGHT);
            case HORIZONTAL -> List.of(LEFT, RIGHT);
            case VERTICAL -> List.of(TOP, BOTTOM);
            default -> List.of(this);
        };

        return actualSides.iterator();
    }
}
