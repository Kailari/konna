package fi.jakojaannos.riista.application.impl;

import java.util.ArrayDeque;
import java.util.Queue;

import fi.jakojaannos.riista.data.events.EventReceiver;
import fi.jakojaannos.riista.data.events.EventSender;

public class EventBus<TEvent> implements EventReceiver<TEvent>, EventSender<TEvent> {
    private final Queue<TEvent> events = new ArrayDeque<>();

    @Override
    public void fire(final TEvent event) {
        synchronized (this.events) {
            this.events.offer(event);
        }
    }

    @Override
    public boolean hasEvents() {
        return !this.events.isEmpty();
    }

    @Override
    public TEvent pollEvent() {
        return this.events.remove();
    }
}
