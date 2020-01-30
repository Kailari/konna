package fi.jakojaannos.roguelite.launcher.arguments.parameters;

import fi.jakojaannos.roguelite.launcher.arguments.ArgumentOutOfBoundsException;
import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;

public class IntegerParameter extends Parameter<Integer> {
    private int minimum;
    private boolean hasMinimum;

    IntegerParameter(final String name) {
        super(name);
    }

    public IntegerParameter withMin(final int minimum) {
        this.minimum = minimum;
        this.hasMinimum = true;
        return this;
    }

    @Override
    public Integer parse(final String string) throws ArgumentParsingException {
        try {
            final var value = Integer.parseInt(string);
            if (this.hasMinimum && value < this.minimum) {
                throw new ArgumentOutOfBoundsException(String.format(
                        "Given %s is out of bounds! Expected minimum of %d, got %d",
                        this.name,
                        this.minimum,
                        value
                ));
            }
            return value;
        } catch (final NumberFormatException e) {
            throw new ArgumentParsingException(String.format(
                    "invalid integer \"%s\" passed as \"%s\"",
                    string,
                    this.name
            ));
        }
    }
}
