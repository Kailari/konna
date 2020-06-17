package fi.jakojaannos.roguelite.engine.utilities.assertions.world.builder;

import java.util.function.Consumer;
import java.util.function.Supplier;

import fi.jakojaannos.riista.GameMode;
import fi.jakojaannos.riista.ecs.SystemDispatcher;
import fi.jakojaannos.riista.ecs.SystemGroup;
import fi.jakojaannos.riista.ecs.SystemState;
import fi.jakojaannos.riista.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationBuilder;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner.SimulationRunnerImpl;

public class SimulationBuilderImpl implements SimulationBuilder {
    private final SystemDispatcher.Builder dispatcherBuilder = SystemDispatcher.builder();
    private Consumer<World> initialStateFactory = world -> {};
    private Consumer<SystemState> systemStateFactory = systemState -> {};

    @Override
    public SimulationBuilder withState(final Consumer<World> builder) {
        this.initialStateFactory = this.initialStateFactory.andThen(builder);
        return this;
    }

    @Override
    public SimulationBuilder withSystemState(final Consumer<SystemState> builder) {
        this.systemStateFactory = this.systemStateFactory.andThen(builder);
        return this;
    }

    @Override
    public SimulationBuilder withSystemGroup(
            final String name,
            final Consumer<SystemGroup.Builder> builder
    ) {
        final var group = this.dispatcherBuilder.group(name);
        builder.accept(group);
        group.buildGroup();
        return this;
    }

    @Override
    public SimulationInspector build() {
        final var dispatcher = this.dispatcherBuilder.build();
        final Supplier<SystemState> systemStateFactory = this.systemStateFactory == null
                ? dispatcher::createDefaultState
                : () -> buildSystemState(dispatcher);
        final var gameMode = new GameMode(0,
                                          dispatcher,
                                          this.initialStateFactory,
                                          systemStateFactory);
        return new SimulationRunnerImpl<>(gameMode, null);
    }

    private SystemState buildSystemState(final SystemDispatcher dispatcher) {
        final var systemState = dispatcher.createDefaultState();
        this.systemStateFactory.accept(systemState);
        return systemState;
    }
}
