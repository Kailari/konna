package fi.jakojaannos.roguelite.engine.input;

import java.util.Optional;
import javax.annotation.Nullable;

public record InputEvent(@Nullable AxialInput axialInput, @Nullable ButtonInput buttonInput) {
    public static InputEvent axial(final AxialInput input) {
        return new InputEvent(input, null);
    }

    public static InputEvent button(final ButtonInput input) {
        return new InputEvent(null, input);
    }

    public Optional<AxialInput> asAxis() {
        return Optional.ofNullable(this.axialInput);
    }

    public Optional<ButtonInput> asButton() {
        return Optional.ofNullable(this.buttonInput);
    }
}
