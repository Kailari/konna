package fi.jakojaannos.roguelite.launcher.arguments;

public class UnknownArgumentException extends ArgumentParsingException {
    public UnknownArgumentException(final String message) {
        super(message);
    }
}
