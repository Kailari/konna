package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

public interface SimulationRunner<TRunner> {
    TRunner runsForTicks(long n);

    TRunner runsForSeconds(double seconds);

    default TRunner runsSingleTick() {
        return runsForTicks(1);
    }
}
