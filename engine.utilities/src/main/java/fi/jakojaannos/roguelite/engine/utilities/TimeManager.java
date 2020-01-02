package fi.jakojaannos.roguelite.engine.utilities;

public interface TimeManager {
    long getTimeStep();

    double getTimeStepInSeconds();

    /**
     * The number of ticks passed since the game started.
     */
    long getCurrentGameTime();

    default double getTicksPerSecond() {
        return 1000.0 / getTimeStep();
    }

    /**
     * Converts the seconds to number of <strong>whole</strong> ticks in that timespan.
     *
     * @param seconds amount of seconds to convert
     *
     * @return the number of ticks the time lasts based on current {@link #getTimeStep() timestep}
     */
    default long convertToTicks(final double seconds) {
        return (long) Math.floor(seconds * getTicksPerSecond());
    }

    /**
     * Coverts the number of ticks to seconds.
     *
     * @param ticks number of ticks to convert
     *
     * @return the time in seconds the ticks take with the current {@link #getTimeStep() timestep}
     */
    default double convertToSeconds(final long ticks) {
        return ticks / getTicksPerSecond();
    }
}
