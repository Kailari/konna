package fi.jakojaannos.riista.data.resources;

import fi.jakojaannos.riista.data.events.EventReceiver;
import fi.jakojaannos.riista.data.events.EventSender;
import fi.jakojaannos.riista.data.events.StateEvent;
import fi.jakojaannos.riista.input.InputEvent;

// TODO: Break this apart. Input events could be system events (?), do something fancier with state events (?), expose single event bus interface for system events
//  - Why? To clean up system dependencies.
public record Events(
        EventReceiver<InputEvent>input,
        EventSender<StateEvent>state,
        EventSender<Object>system
) {}
