package fi.jakojaannos.roguelite.engine.ecs.newimpl.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.EcsSystem;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(final String[] args) {
        final List<EcsSystem> systems = List.of(new MutateSystem(),
                                                new PrintSystem());

        final var componentAs = new SampleComponentA[]{
                new SampleComponentA(10),
                new SampleComponentA(5),
                new SampleComponentA(0),
                new SampleComponentA(Integer.MAX_VALUE),
                new SampleComponentA(42),
        };
        final var componentBs = new SampleComponentB[]{
                new SampleComponentB(-1),
                new SampleComponentB(1),
                new SampleComponentB(2),
                new SampleComponentB(Short.MIN_VALUE),
                null,
        };

        final int entityCount = 5;
        for (int i = 0; i < 2; i++) {
            LOG.info("Tick #{}", i);
            dispatch(systems, componentAs, componentBs, entityCount);
        }
    }

    private static void dispatch(
            final Iterable<EcsSystem> systems,
            final SampleComponentA[] componentAs,
            final SampleComponentB[] componentBs,
            final int entityCount
    ) {
        for (final var system : systems) {
            final var requirements = system.declareRequirements();

            final var componentTypes = requirements.getComponentTypes();
            final var paramStorages = fetchStorages(componentAs, componentBs, componentTypes);

            final var parameters = new Object[componentTypes.length];
            for (int i = 0; i < entityCount; ++i) {
                final var incomplete = tryFetchEntityData(paramStorages, parameters, i);
                if (incomplete) {
                    continue;
                }

                final Object entityData;
                try {
                    entityData = requirements.getConstructor().newInstance(parameters);
                } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalStateException("Could not instantiate entity data!");
                }

                system.tick(new EcsSystem.NoResources(),
                            Stream.of(new EntityDataHandleImpl<>(entityData)),
                            new EcsSystem.NoEvents());
            }
        }
    }

    private static boolean tryFetchEntityData(final Object[][] paramStorages, final Object[] parameters, final int i) {
        for (int paramIndex = 0; paramIndex < parameters.length; ++paramIndex) {
            parameters[paramIndex] = paramStorages[paramIndex][i];

            if (parameters[paramIndex] == null) {
                return true;
            }
        }

        return false;
    }

    private static Object[][] fetchStorages(
            final SampleComponentA[] componentAs,
            final SampleComponentB[] componentBs,
            final Class<?>[] componentTypes
    ) {
        final var paramStorages = new Object[componentTypes.length][];

        for (int paramIndex = 0; paramIndex < paramStorages.length; ++paramIndex) {
            final Class<?> paramType = componentTypes[paramIndex];

            if (paramType.isAssignableFrom(SampleComponentA.class)) {
                paramStorages[paramIndex] = componentAs;
            } else if (paramType.isAssignableFrom(SampleComponentB.class)) {
                paramStorages[paramIndex] = componentBs;
            } else {
                throw new IllegalStateException("Unknown parameter type!");
            }
        }
        return paramStorages;
    }

    private static class EntityDataHandleImpl<TEntityData> implements EcsSystem.EntityDataHandle<TEntityData> {
        private final TEntityData data;

        @Override
        public TEntityData getData() {
            return this.data;
        }

        private EntityDataHandleImpl(final TEntityData data) {
            this.data = data;
        }

        @Override
        public <TComponent> boolean addComponent(final TComponent component) {
            return false;
        }

        @Override
        public <TComponent> boolean removeComponent(final Class<TComponent> componentClass) {
            return false;
        }

        @Override
        public <TComponent> boolean hasComponent(final Class<TComponent> componentClass) {
            return false;
        }

        @Override
        public void destroy() {

        }
    }
}
