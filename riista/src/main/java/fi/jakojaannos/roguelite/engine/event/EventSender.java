package fi.jakojaannos.roguelite.engine.event;

public interface EventSender<TEvent> {
    void fire(TEvent event);
}
