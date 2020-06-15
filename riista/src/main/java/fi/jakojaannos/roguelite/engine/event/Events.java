package fi.jakojaannos.roguelite.engine.event;

import fi.jakojaannos.roguelite.engine.input.InputEvent;
import fi.jakojaannos.riista.data.events.StateEvent;
import fi.jakojaannos.riista.data.events.UiEvent;

// FIXME: Move everything to a single system event bus and receive all events via system events
@Deprecated
public record Events(
        EventReceiver<UiEvent>ui,
        EventReceiver<InputEvent>input,
        EventSender<StateEvent>state,
        EventSender<Object>system
) {}
