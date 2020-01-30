package fi.jakojaannos.roguelite.engine.event;

import lombok.Getter;

import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;

public class Events implements Resource {
    @Getter private final EventBus<UIEvent> ui = new EventBus<>();
    @Getter private final EventBus<InputEvent> input = new EventBus<>();
}
