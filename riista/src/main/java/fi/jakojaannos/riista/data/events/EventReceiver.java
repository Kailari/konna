package fi.jakojaannos.riista.data.events;

public interface EventReceiver<TEvent> {
    boolean hasEvents();

    TEvent pollEvent();
}
