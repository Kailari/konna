package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.GameState;

/**
 * Allows <i>inspecting</i> the simulation state. E.g. performing assertions over the game state or performing manual
 * state manipulations.
 */
public interface SimulationInspector extends SimulationRunner<SimulationInspector> {
    /**
     * Perform some assertion over current game state or perform some state transformations.
     *
     * @param expectation assertions over expected outcome or some desired state transformation
     *
     * @return self for chaining
     */
    SimulationInspector expect(Consumer<GameState> expectation);

    /**
     * Alias for {@link #expect(Consumer)}
     *
     * @param expectation assertions over expected outcome or some desired state transformation
     *
     * @return self for chaining
     */
    default SimulationInspector then(final Consumer<GameState> expectation) {
        return expect(expectation);
    }
}
