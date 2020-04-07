package fi.jakojaannos.roguelite.engine.state;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;

public interface StateEvent {
    record ChangeMode(GameMode gameMode) implements StateEvent {}

    record ChangeState(GameState gameState) implements StateEvent {}

    record Shutdown() implements StateEvent {}
}
