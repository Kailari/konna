package fi.jakojaannos.roguelite.engine.utilities.assertions.world.builder;

import java.util.function.Consumer;

import fi.jakojaannos.roguelite.engine.GameMode;
import fi.jakojaannos.roguelite.engine.GameState;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationBuilder;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationInspector;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.SimulationRunner;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner.SimulationRunnerImpl;
import fi.jakojaannos.roguelite.engine.utilities.assertions.world.runner.TestGameRunner;

public class SimulationBuilderImpl implements SimulationBuilder, SimulationRunner<SimulationInspector> {
    private final SystemDispatcher.Builder dispatcherBuilder = SystemDispatcher.builder();
    private Consumer<World> initialStateFactory = world -> {};

    @Override
    public SimulationBuilder withState(final Consumer<World> builder) {
        this.initialStateFactory = this.initialStateFactory.andThen(builder);
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
    public SimulationInspector runsForSingleTick() {
        return build().runsForSingleTick();
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
        final var gameMode = new GameMode(0,
                                          this.dispatcherBuilder.build(),
                                          world -> {
                                              this.initialStateFactory.accept(world);
                                              return new GameState(world);
                                          });
        return new SimulationRunnerImpl(new TestGameRunner(gameMode));
    }
}
