package fi.jakojaannos.roguelite.launcher.arguments.parameters;

import fi.jakojaannos.roguelite.launcher.arguments.ArgumentOutOfBoundsException;
import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;
import lombok.val;

public class IntegerParameter extends Parameter<Integer> {
    private int minimum;
    private boolean hasMinimum = false;

    IntegerParameter(String name) {
        super(name);
    }


    public fi.jakojaannos.roguelite.launcher.arguments.parameters.IntegerParameter withMin(int minimum) {
        this.minimum = minimum;
        this.hasMinimum = true;
        return this;
    }


    @Override
    public Integer parse(String string) throws ArgumentParsingException {
        try {
            val value = Integer.parseInt(string);
            if (this.hasMinimum && value < this.minimum) {
                throw new ArgumentOutOfBoundsException(String.format(
                        "Given %s is out of bounds! Expected minimum of %d, got %d",
                        this.name,
                        this.minimum,
                        value
                ));
            }
            return value;
        } catch (NumberFormatException e) {
            throw new ArgumentParsingException(String.format(
                    "Invalid integer \"%s\" passed as \"%s\"",
                    string,
                    this.name
            ));
        }
    }
}
