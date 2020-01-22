package fi.jakojaannos.roguelite.engine.input;

import java.util.Queue;

public interface InputProvider {
    /**
     * Gets all input events gathered since last frame.
     *
     * @return queue containing all input events to be processed
     */
    Queue<InputEvent> pollEvents();
}
