package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.event.Events;
import fi.jakojaannos.roguelite.engine.state.GameState;

public interface GameRenderer extends AutoCloseable {
    void render(GameState state, double partialTickAlpha, Events events);
}
