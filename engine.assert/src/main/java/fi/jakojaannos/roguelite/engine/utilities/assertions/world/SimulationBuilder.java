package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.Arrays;
import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.SystemState;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;

public interface SimulationBuilder extends SimulationRunner<SimulationInspector> {
    SimulationBuilder withState(Consumer<World> builder);

    SimulationBuilder withSystemState(Consumer<SystemState> builder);

    SimulationBuilder withSystemGroup(
            String name,
            Consumer<SystemGroup.Builder> groupBuilder
    );

    @SuppressWarnings("rawtypes")
    default SimulationBuilder withSystems(final EcsSystem... systems) {
        return withSystemGroup("default-test-group",
                               builder -> Arrays.stream(systems).forEach(builder::withSystem));
    }

    @Deprecated
    default SimulationBuilder withSystems(final ECSSystem... systems) {
        return withSystemGroup("default-test-group",
                               builder -> Arrays.stream(systems).forEach(builder::withSystem));
    }
}
