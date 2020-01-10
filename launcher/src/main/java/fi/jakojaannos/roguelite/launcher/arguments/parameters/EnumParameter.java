package fi.jakojaannos.roguelite.launcher.arguments.parameters;

import fi.jakojaannos.roguelite.launcher.arguments.ArgumentParsingException;

public class EnumParameter<T extends Enum<T>> extends Parameter<T> {
    private final Class<T> type;

    EnumParameter(String name, Class<T> type) {
        super(name);
        this.type = type;
    }


    @Override
    public T parse( String string) throws ArgumentParsingException {
        try {
            return T.valueOf(this.type, string);
        } catch (IllegalArgumentException e) {
            throw new ArgumentParsingException("String \"" + string + "\" does not represent valid constant of the enum \"" + this.type.getSimpleName() + "\"", e);
        }
    }
}
