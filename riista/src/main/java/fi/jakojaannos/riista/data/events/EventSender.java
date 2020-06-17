package fi.jakojaannos.riista.data.events;

public interface EventSender<TEvent> {
    void fire(TEvent event);
}
