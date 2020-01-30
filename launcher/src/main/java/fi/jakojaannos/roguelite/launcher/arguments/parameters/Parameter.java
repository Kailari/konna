package fi.jakojaannos.roguelite.launcher.arguments.parameters;

import lombok.RequiredArgsConstructor;

import java.util.Optional;

import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;

@RequiredArgsConstructor
public abstract class Parameter<T> {

    protected final String name;

    public static <T> Parameter<Optional<T>> optional(final Parameter<T> parameter) {
        return new Parameter<>(parameter.name) {
            @Override
            public Optional<T> parse(final String string) {
                try {
                    return Optional.ofNullable(parameter.parse(string));
                } catch (final ArgumentParsingException ignored) {
                    return Optional.empty();
                }
            }
        };
    }

    public static StringParameter string(final String name) {
        return new StringParameter(name);
    }

    public static IntegerParameter integer(final String name) {
        return new IntegerParameter(name);
    }

    public static <T extends Enum<T>> EnumParameter<T> enumeration(
            final String name,
            final Class<T> enumClass
    ) {
        return new EnumParameter<>(name, enumClass);
    }

    public static BoolParameter bool(final String name) {
        return new BoolParameter(name);
    }

    public static PathParameter filePath(final String name) {
        return new PathParameter(name);
    }

    public abstract T parse(String string) throws ArgumentParsingException;
}
