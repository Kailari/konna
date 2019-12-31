package fi.jakojaannos.roguelite.engine.event;

import java.util.ArrayDeque;
import java.util.Queue;

public class EventBus<TEvent> {
    private final Queue<TEvent> events = new ArrayDeque<>();

    public void fire(final TEvent event) {
        this.events.offer(event);
    }

    public boolean hasEvents() {
        return !this.events.isEmpty();
    }

    public TEvent pollEvent() {
        return this.events.remove();
    }
}
