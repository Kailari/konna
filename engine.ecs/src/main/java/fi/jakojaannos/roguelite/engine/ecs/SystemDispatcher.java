package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.systems.DispatcherBuilderImpl;
import lombok.NonNull;

import java.util.stream.Stream;

/**
 * Dispatcher for executing system ticks in a controlled manner. Dispatches {@link
 * ECSSystem#tick(Stream, World, double)} for all systems registered for this dispatcher,
 * automagically respecting system dependencies.
 */
public interface SystemDispatcher extends AutoCloseable {
    static DispatcherBuilder builder() {
        return new DispatcherBuilderImpl();
    }

    void dispatch(@NonNull World world, double delta);
}
