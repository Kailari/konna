package fi.jakojaannos.roguelite.launcher.arguments.parameters;

import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class Parameter<T> {

    protected final String name;

    public abstract T parse(String string) throws ArgumentParsingException;

    public static <T> Parameter<Optional<T>> optional(Parameter<T> parameter) {
        return new Parameter<>(parameter.name) {
            @Override
            public Optional<T> parse(final String string) {
                try {
                    return Optional.ofNullable(parameter.parse(string));
                } catch (ArgumentParsingException ignored) {
                    return Optional.empty();
                }
            }
        };
    }

    public static StringParameter string(String name) {
        return new StringParameter(name);
    }

    public static IntegerParameter integer(String name) {
        return new IntegerParameter(name);
    }


    public static <T extends Enum<T>> EnumParameter<T> enumeration(
            String name,
            Class<T> enumClass
    ) {
        return new EnumParameter<>(name, enumClass);
    }

    public static BoolParameter bool(String name) {
        return new BoolParameter(name);
    }

    public static PathParameter filePath(String name) {
        return new PathParameter(name);
    }
}
