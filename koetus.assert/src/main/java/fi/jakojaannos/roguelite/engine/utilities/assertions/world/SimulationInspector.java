package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.input.InputEvent;

/**
 * Allows <i>inspecting</i> the simulation state. E.g. performing assertions over the game state or performing manual
 * state manipulations.
 */
public interface SimulationInspector extends SimulationRunner<SimulationInspector> {
    /**
     * Checks if the simulation has terminated.
     *
     * @return <code>true</code> if the simulation has terminated
     */
    boolean isTerminated();

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

    /**
     * Gets the current game state for the simulation. This is the state that would be passed to {@link
     * #expect(Consumer) expect}.
     *
     * @return the current game state
     */
    GameState state();

    /**
     * Gets the current game mode for the simulation. This is the latest game mode the simulation was in when ticking.
     *
     * @return the current game mode
     */
    GameMode mode();

    Queue<InputEvent> inputQueue();
}
