package fi.jakojaannos.roguelite.engine.ecs.systemdata;

public class IllegalRequirementsException extends RuntimeException {
    public IllegalRequirementsException(final String message) {
        super(message);
    }
}
