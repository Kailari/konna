package fi.jakojaannos.roguelite.engine.event;

import lombok.Getter;

import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;

public class Events implements ProvidedResource {
    @Getter private final EventBus<UIEvent> ui = new EventBus<>();
    @Getter private final EventBus<InputEvent> input = new EventBus<>();
}
