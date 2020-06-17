package fi.jakojaannos.riista.data.events;

import fi.jakojaannos.riista.GameMode;

public interface StateEvent {
    record ChangeMode(GameMode gameMode) implements StateEvent {}

    record Shutdown() implements StateEvent {}
}
