package fi.jakojaannos.roguelite.engine.view;

import fi.jakojaannos.roguelite.engine.event.Events;

public interface GameRenderer<TState> extends AutoCloseable {
    void render(TState state, double partialTickAlpha, Events events);
}
