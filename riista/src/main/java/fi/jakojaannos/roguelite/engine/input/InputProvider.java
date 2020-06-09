package fi.jakojaannos.roguelite.engine.input;

public interface InputProvider {
    /**
     * Gets all input events gathered since last frame.
     *
     * @return queue containing all input events to be processed
     */
    Iterable<InputEvent> pollEvents();
}
