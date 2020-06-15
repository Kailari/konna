package fi.jakojaannos.riista.data.events;

import fi.jakojaannos.roguelite.engine.GameMode;

public interface StateEvent {
    record ChangeMode(GameMode gameMode) implements StateEvent {}

    record Shutdown() implements StateEvent {}
}
