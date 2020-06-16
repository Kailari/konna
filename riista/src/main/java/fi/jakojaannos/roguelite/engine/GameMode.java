package fi.jakojaannos.roguelite.engine;

import java.util.function.Consumer;
import java.util.function.Supplier;

import fi.jakojaannos.riista.ecs.SystemDispatcher;
import fi.jakojaannos.riista.ecs.SystemState;
import fi.jakojaannos.riista.ecs.World;

public record GameMode(
        int id,
        SystemDispatcher systemDispatcher,
        Consumer<World>stateFactory,
        Supplier<SystemState>systemStateFactory
) implements AutoCloseable {
    public GameMode(
            final int id,
            final SystemDispatcher systemDispatcher,
            final Consumer<World> stateFactory
    ) {
        this(id, systemDispatcher, stateFactory, systemDispatcher::createDefaultState);
    }

    @Override
    public void close() throws Exception {
        this.systemDispatcher.close();
    }
}
