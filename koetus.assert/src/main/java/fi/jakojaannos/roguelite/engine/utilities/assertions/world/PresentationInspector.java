package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.GameState;

public interface PresentationInspector<TPresentState> extends SimulationInspector {
    @Override
    PresentationInspector<TPresentState> expect(Consumer<GameState> expectation);

    @Override
    default PresentationInspector<TPresentState> then(final Consumer<GameState> expectation) {
        return expect(expectation);
    }

    @Override
    PresentationInspector<TPresentState> runsForTicks(long n);

    @Override
    PresentationInspector<TPresentState> runsForSeconds(double seconds);

    @Override
    default PresentationInspector<TPresentState> runsSingleTick() {
        return runsForTicks(1);
    }
}
