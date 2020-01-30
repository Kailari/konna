package fi.jakojaannos.roguelite.launcher.arguments;

public class ArgumentParsingException extends Exception {
    public ArgumentParsingException(final String message) {
        super(message);
    }

    public ArgumentParsingException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
