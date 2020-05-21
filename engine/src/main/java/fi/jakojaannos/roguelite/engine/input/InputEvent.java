package fi.jakojaannos.roguelite.engine.input;

import java.util.Optional;
import javax.annotation.Nullable;

public record InputEvent(@Nullable AxialInput axialInput, @Nullable ButtonInput buttonInput) {
    @Deprecated
    public static InputEvent axial(final AxialInput input) {
        return new InputEvent(input, null);
    }

    @Deprecated
    public static InputEvent button(final ButtonInput input) {
        return new InputEvent(null, input);
    }

    public static InputEvent axis(final InputAxis axis, final double value) {
        return new InputEvent(new AxialInput(axis, value), null);
    }

    public static InputEvent button(final InputButton button, final ButtonInput.Action action) {
        return new InputEvent(null, new ButtonInput(button, action));
    }

    public Optional<AxialInput> asAxis() {
        return Optional.ofNullable(this.axialInput);
    }

    public Optional<ButtonInput> asButton() {
        return Optional.ofNullable(this.buttonInput);
    }
}
