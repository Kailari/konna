package fi.jakojaannos.roguelite.engine.ecs;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.systems.DispatcherBuilderImpl;

/**
 * Dispatcher for executing system ticks in a controlled manner. Dispatches {@link ECSSystem#tick(Stream, World,
 * double)} for all systems registered for this dispatcher, automagically respecting system dependencies.
 */
public interface SystemDispatcher extends AutoCloseable {
    static DispatcherBuilder builder() {
        return new DispatcherBuilderImpl();
    }

    /**
     * Call this to tick all registered systems.
     *
     * @param world ECS {@link World} to use as a data source for the systems.
     */
    void dispatch(World world);
}
