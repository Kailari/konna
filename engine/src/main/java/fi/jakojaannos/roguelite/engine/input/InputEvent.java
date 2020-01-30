package fi.jakojaannos.roguelite.engine.input;

import java.util.Optional;

public class InputEvent {
    private final AxialInput axialInput;
    private final ButtonInput buttonInput;

    public InputEvent(final AxialInput input) {
        this.axialInput = input;
        this.buttonInput = null;
    }

    public InputEvent(final ButtonInput input) {
        this.axialInput = null;
        this.buttonInput = input;
    }

    public Optional<AxialInput> getAxis() {
        return Optional.ofNullable(this.axialInput);
    }

    public Optional<ButtonInput> getButton() {
        return Optional.ofNullable(this.buttonInput);
    }

}
