package fi.jakojaannos.roguelite.engine.utilities.assertions.world.builder;

import java.util.function.Consumer;
import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.SystemState;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationBuilder;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner.SimulationRunnerImpl;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner.TestGameRunner;

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
    public SimulationInspector runsSingleTick() {
        return build().runsSingleTick();
    }

    @Override
    public SimulationInspector runsForTicks(final long n) {
        return build().runsForTicks(n);
    }

    @Override
    public SimulationInspector runsForSeconds(final double seconds) {
        return build().runsForSeconds(seconds);
    }

    private SimulationInspector build() {
        final var dispatcher = this.dispatcherBuilder.build();
        final Supplier<SystemState> systemStateFactory = this.systemStateFactory == null
                ? dispatcher::createDefaultState
                : () -> buildSystemState(dispatcher);
        final var gameMode = new GameMode(0,
                                          dispatcher,
                                          this.initialStateFactory,
                                          systemStateFactory);
        return new SimulationRunnerImpl(new TestGameRunner(gameMode));
    }

    private SystemState buildSystemState(final SystemDispatcher dispatcher) {
        final var systemState = dispatcher.createDefaultState();
        this.systemStateFactory.accept(systemState);
        return systemState;
    }
}
