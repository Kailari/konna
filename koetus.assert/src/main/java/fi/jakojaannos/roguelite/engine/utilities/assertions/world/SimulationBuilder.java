package fi.jakojaannos.roguelite.engine.utilities.assertions.world;

import java.util.Arrays;
import java.util.function.Consumer;

import fi.jakojaannos.riista.ecs.EcsSystem;
import fi.jakojaannos.riista.ecs.SystemGroup;
import fi.jakojaannos.riista.ecs.SystemState;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.riista.input.InputEvent;

public interface SimulationBuilder extends SimulationRunner<SimulationInspector> {
    SimulationBuilder withState(Consumer<World> builder);

    SimulationBuilder withSystemState(Consumer<SystemState> builder);

    SimulationBuilder withSystemGroup(
            String name,
            Consumer<SystemGroup.Builder> groupBuilder
    );

    /**
     * Manually builds the simulation. Use this if you for some reason need to make assertions on the initial state of
     * the simulation. Otherwise, prefer {@link #runsForTicks(long) runsForXXX(...)} -methods, which automatically build
     * the simulation and run simulation for specified time.
     *
     * @return the built simulation inspector
     */
    SimulationInspector build();

    @SuppressWarnings("rawtypes")
    default SimulationBuilder withSystems(final EcsSystem... systems) {
        return withSystemGroup("default-test-group",
                               builder -> Arrays.stream(systems).forEach(builder::withSystem));
    }

    @Override
    default SimulationInspector runsForTicks(final long n) {
        return build().runsForTicks(n);
    }

    @Override
    default SimulationInspector runsForSeconds(final double seconds) {
        return build().runsForSeconds(seconds);
    }

    @Override
    default SimulationInspector skipsTicks(final int n) {
        return build().skipsTicks(n);
    }

    @Override
    default SimulationInspector receivesInput(final InputEvent button) {
        return build().receivesInput(button);
    }

    @Override
    default SimulationInspector setCurrentTickAsSeconds(final double seconds) {
        return build().setCurrentTickAsSeconds(seconds);
    }
}
