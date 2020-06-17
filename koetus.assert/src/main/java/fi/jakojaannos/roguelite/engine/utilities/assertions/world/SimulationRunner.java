package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import fi.jakojaannos.riista.input.InputEvent;

public interface SimulationRunner<TRunner> {
    TRunner runsForTicks(long n);

    TRunner runsForSeconds(double seconds);

    default TRunner runsSingleTick() {
        return runsForTicks(1);
    }

    /**
     * Increments the current time by given number of ticks without ticking any systems. Use sparingly as this has
     * potential to e.g. skips ticks which would trigger timestamp-based timers etc.
     *
     * @param n the number of ticks to skip
     *
     * @return self for chaining
     */
    TRunner skipsTicks(int n);

    TRunner receivesInput(InputEvent button);
}
