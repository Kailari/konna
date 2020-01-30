package fi.jakojaannos.roguelite.launcher.arguments;

import lombok.AccessLevel;
import lombok.Getter;

import fi.jakojaannos.roguelite.launcher.arguments.parameters.Parameter;

public class ArgumentParameters {
    private final int beginIndex;
    private final String[] args;

    @Getter(AccessLevel.PACKAGE) private int consumed;

    ArgumentParameters(final int beginIndex, final String[] args) {
        this.beginIndex = beginIndex;
        this.args = args;
    }

    public <T> T parameter(final Parameter<T> parameter) throws ArgumentParsingException {
        ++this.consumed;
        if (this.consumed >= this.args.length) {
            throw new ArgumentParsingException("Not enough parameters provided");
        }

        final var str = this.args[this.beginIndex + (this.consumed - 1)];
        if (str.startsWith("-")) {
            throw new ArgumentParsingException(String.format(
                    "Not enough parameters provided. Got next argument (%s) as parameter string " +
                            "while parsing!",
                    str
            ));
        }
        return parameter.parse(str);
    }
}
