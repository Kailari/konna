package fi.jakojaannos.roguelite.engine.event;

import java.util.Queue;

public record RenderEvents(Queue<Object>events) {
    public void fire(final Object event) {
        this.events().add(event);
    }
}
