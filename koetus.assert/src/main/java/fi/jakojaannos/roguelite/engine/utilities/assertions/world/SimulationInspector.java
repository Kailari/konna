package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.GameState;

public interface SimulationInspector extends SimulationRunner<SimulationInspector> {
    SimulationInspector expect(Consumer<GameState> expectation);
}
