package fi.jakojaannos.roguelite.engine;

import java.util.function.Function;

import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;

public record GameMode(
        int id,
        SystemDispatcher systemDispatcher,
        Function<World, GameState>stateFactory
) implements AutoCloseable {
    @Override
    public void close() throws Exception {
        this.systemDispatcher.close();
    }
}
