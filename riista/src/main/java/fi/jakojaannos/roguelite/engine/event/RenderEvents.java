package fi.jakojaannos.roguelite.engine.event;

import java.util.Queue;

@Deprecated
public record RenderEvents(Queue<Object>events) {
    public void fire(final Object event) {
        this.events().add(event);
    }
}
