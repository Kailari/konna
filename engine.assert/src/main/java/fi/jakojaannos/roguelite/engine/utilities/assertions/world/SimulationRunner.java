package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

public interface SimulationRunner<TRunner> {
    TRunner runsForTicks(long n);

    TRunner runsForSeconds(double seconds);

    default TRunner runsForSingleTick() {
        return runsForTicks(1);
    }
}
