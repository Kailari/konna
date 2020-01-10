package fi.jakojaannos.roguelite.launcher.arguments;

import fi.jakojaannos.roguelite.launcher.arguments.parameters.Parameter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;

public class ArgumentParameters {
    private final int beginIndex;
    private final  String[] args;

    @Getter(AccessLevel.PACKAGE) private int consumed = 0;

    ArgumentParameters(int beginIndex,  String[] args) {
        this.beginIndex = beginIndex;
        this.args = args;
    }


    public <T> T parameter( Parameter<T> parameter) throws ArgumentParsingException {
        ++this.consumed;
        if (this.consumed >= this.args.length) {
            throw new ArgumentParsingException("Not enough parameters provided");
        }

        val str = this.args[this.beginIndex + (this.consumed - 1)];
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
