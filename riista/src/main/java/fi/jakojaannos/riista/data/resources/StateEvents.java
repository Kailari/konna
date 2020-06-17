package fi.jakojaannos.riista.data.resources;

import fi.jakojaannos.riista.data.events.EventSender;
import fi.jakojaannos.riista.data.events.StateEvent;

public interface StateEvents extends EventSender<StateEvent> {}
