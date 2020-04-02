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

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        final List<EcsSystem<?, ?, ?>> systems = List.of(new AdderSystem(),
                                                         new PrintSystem());

final var componentAs = new ValueComponent[]{
        new ValueComponent(10),
        new ValueComponent(5),
        new ValueComponent(42),
        new ValueComponent(0),
        new ValueComponent(Integer.MAX_VALUE),
};
final var componentBs = new AmountComponent[]{
        new AmountComponent(-1),
        new AmountComponent(1),
        null,
        new AmountComponent(2),
        new AmountComponent(Short.MIN_VALUE),
};

        final int entityCount = 5;
        LOG.info("Initial state");
        dispatch(componentAs, componentBs, entityCount, systems.get(1));

        final var ticksToRun = 2;
        for (int i = 0; i < ticksToRun; i++) {
            LOG.info("Tick #{}", i);
            for (final EcsSystem<?, ?, ?> system : systems) {
                dispatch(componentAs, componentBs, entityCount, system);
            }
        }
    }

    private static <TResources, TEntityData, TEvents> void dispatch(
            final ValueComponent[] componentAs,
            final AmountComponent[] componentBs,
            final int entityCount,
            final EcsSystem<TResources, TEntityData, TEvents> system
    ) {
        final var requirements = system.declareRequirements();

        final var componentTypes = requirements.getEntityDataComponentTypes();
        final var paramStorages = fetchStorages(componentAs, componentBs, componentTypes);

        final var constructor = requirements.getEntityDataConstructor();
        final var entitySpliterator = new EntitySpliterator<>(paramStorages,
                                                              entityCount,
                                                              createEntityDataFactory(constructor));

        final ForkJoinPool threadPool = new ForkJoinPool(4,
                                                         Main::workerThreadFactory,
                                                         null,
                                                         false);
        try {
            threadPool.submit(
                    () -> system.tick(null,
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

    private static Object[][] fetchStorages(
            final ValueComponent[] componentAs,
            final AmountComponent[] componentBs,
            final Class<?>[] componentTypes
    ) {
        final var paramStorages = new Object[componentTypes.length][];

        for (int paramIndex = 0; paramIndex < paramStorages.length; ++paramIndex) {
            final Class<?> paramType = componentTypes[paramIndex];

            if (paramType.isAssignableFrom(ValueComponent.class)) {
                paramStorages[paramIndex] = componentAs;
            } else if (paramType.isAssignableFrom(AmountComponent.class)) {
                paramStorages[paramIndex] = componentBs;
            } else {
                throw new IllegalStateException("Unknown parameter type!");
            }
        }
        return paramStorages;
    }
}
