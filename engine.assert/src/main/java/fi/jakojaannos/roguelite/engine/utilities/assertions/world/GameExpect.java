package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import fi.jakojaannos.roguelite.engine.utilities.assertions.world.builder.SimulationBuilderImpl;

public final class GameExpect {
    public static SimulationBuilder whenGame() {
        return new SimulationBuilderImpl();
    }
}
