package fi.jakojaannos.roguelite.engine.ecs;

import fi.jakojaannos.roguelite.engine.ecs.dispatcher.SystemDispatcherBuilderImpl;

public interface SystemDispatcher extends AutoCloseable {
    static Builder builder() {
        return new SystemDispatcherBuilderImpl();
    }

    void tick(World world);

    interface Builder {
        SystemDispatcher build();

        SystemGroup.Builder group(String name);

        default SystemGroup.Builder group() {
            return group("UNNAMED");
        }
    }
}
