package fi.jakojaannos.roguelite.engine.event;

import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;

public record Events(EventBus<UIEvent>ui, EventBus<InputEvent>input) implements ProvidedResource {
    public Events() {
        this(new EventBus<>(), new EventBus<>());
    }
}
