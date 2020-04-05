package fi.jakojaannos.roguelite.engine.ecs.newimpl.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.World;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        final List<EcsSystem<?, ?, ?>> systems = List.of(new AdderSystem(),
                                                         new PrintSystem());

        final var world = World.createNew();
        world.registerResource(new Multiplier(2));

        world.createEntity(new ValueComponent(10), new AmountComponent(-1));
        world.createEntity(new ValueComponent(5), new AmountComponent(1));
        world.createEntity(new ValueComponent(42));
        world.createEntity(new ValueComponent(0), new AmountComponent(2));
        world.createEntity(new ValueComponent(Integer.MAX_VALUE), new AmountComponent(Short.MIN_VALUE));

        world.commitEntityModifications();
        final var threadPool = new ForkJoinPool(4,
                                                Main::workerThreadFactory,
                                                null,
                                                false);
        LOG.info("Initial state");
        dispatch(world, systems.get(1), threadPool);

        final var ticksToRun = 2;
        for (int i = 0; i < ticksToRun; i++) {
            LOG.info("Tick #{}", i);
            for (final EcsSystem<?, ?, ?> system : systems) {
                dispatch(world, system, threadPool);
            }
        }
    }

    private static <TResources, TEntityData, TEvents> void dispatch(
            final World world,
            final EcsSystem<TResources, TEntityData, TEvents> system,
            final ForkJoinPool threadPool
    ) {
        final var requirements = system.declareRequirements();

        final var systemResources = instantiateResources(
                world.getResources().fetch(requirements.resourceComponentTypes()),
                requirements.resourceConstructor()
        );
        final var entitySpliterator = new EntitySpliterator<>(
                requirements.entityDataComponentTypes(),
                world,
                createEntityDataFactory(requirements.entityDataConstructor())
        );

        try {
            threadPool.submit(
                    () -> system.tick(systemResources,
                                      StreamSupport.stream(entitySpliterator, true),
                                      null)
            ).get();
        } catch (final InterruptedException e) {
            LOG.warn("System \"" + system.getClass().getSimpleName() + "\" was interrupted!");
        } catch (final ExecutionException e) {
            throw new IllegalStateException("System \"" + system.getClass().getSimpleName() + "\" failure!", e);
        }
    }

    private static ForkJoinWorkerThread workerThreadFactory(final ForkJoinPool pool) {
        final var worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("ecs-worker-" + worker.getPoolIndex());
        return worker;
    }

    private static <TResources> TResources instantiateResources(
            final Object[] resources,
            final Constructor<TResources> constructor
    ) {
        try {
            return constructor.newInstance(resources);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException("Could not instantiate resources!");
        }
    }

    private static <TEntityData> Function<Object[], TEntityData> createEntityDataFactory(
            final Constructor<TEntityData> constructor
    ) {
        return (params) -> {
            try {
                return constructor.newInstance(params);
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("Could not instantiate entity data!");
            }
        };
    }
}
