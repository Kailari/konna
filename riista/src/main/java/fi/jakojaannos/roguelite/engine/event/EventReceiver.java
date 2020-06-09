package fi.jakojaannos.roguelite.engine.event;

public interface EventReceiver<TEvent> {
    boolean hasEvents();

    TEvent pollEvent();
}
