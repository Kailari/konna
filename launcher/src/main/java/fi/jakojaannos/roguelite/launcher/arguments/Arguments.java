package fi.jakojaannos.roguelite.launcher.arguments;

import java.util.ArrayList;
import java.util.List;

public class Arguments {
    private final List<Argument> arguments = new ArrayList<>();
    private boolean ignoreUnknown;

    public static Arguments builder() {
        return new Arguments();
    }

    public Arguments ignoreUnknown() {
        this.ignoreUnknown = true;
        return this;
    }

    public Arguments with(final Argument argument) {
        this.arguments.add(argument);
        return this;
    }

    public void consume(final String... args) throws ArgumentParsingException {
        for (int i = 0; i < args.length; ++i) {
            final var argStr = args[i];
            if (argStr.isEmpty()) {
                throw new ArgumentParsingException("Argument cannot be empty!");
            } else if (argStr.equals("-") || argStr.equals("--")) {
                throw new ArgumentParsingException("Hyphen should be followed by argument name.");
            }

            final String argName;
            if (argStr.startsWith("--")) {
                argName = argStr.substring(2);
            } else if (argStr.startsWith("-")) {
                argName = argStr.substring(1);
            } else {
                if (this.ignoreUnknown) {
                    continue;
                }

                throw new ArgumentParsingException(String.format(
                        "Got parameter \"%s\", when expecting an argument",
                        argStr
                ));
            }

            final var params = new ArgumentParameters(i + 1, args);
            final var argument = this.arguments.stream()
                                               .filter(a -> a.nameMatches(argName))
                                               .findFirst();

            if (argument.isPresent()) {
                argument.get().consumeArguments(params);
                i += params.getConsumed();
            } else if (!this.ignoreUnknown) {
                throw new UnknownArgumentException(String.format(
                        "Could not find argument with name or alias \"%s\"",
                        argName
                ));
            }
        }
    }
}
