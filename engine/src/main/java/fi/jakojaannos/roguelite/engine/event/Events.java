package fi.jakojaannos.roguelite.engine.event;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.roguelite.engine.ui.UIEvent;
import lombok.Getter;

public class Events {
    @Getter private final EventBus<UIEvent> ui = new EventBus<>();
    @Getter private final EventBus<InputEvent> input = new EventBus<>();
}
