package fi.jakojaannos.roguelite.engine.event;

import java.util.ArrayDeque;
import java.util.Queue;

public class EventBus<TEvent> implements EventReceiver<TEvent>, EventSender<TEvent> {
    private final Queue<TEvent> events = new ArrayDeque<>();

    @Override
    public void fire(final TEvent event) {
        this.events.offer(event);
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
