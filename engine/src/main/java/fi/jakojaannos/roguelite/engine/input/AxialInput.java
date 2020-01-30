package fi.jakojaannos.roguelite.engine.input;

import lombok.Getter;

public class AxialInput {
    @Getter private final InputAxis axis;
    @Getter private final double value;

    public AxialInput(final InputAxis axis, final double value) {
        this.axis = axis;
        this.value = value;
    }
}
