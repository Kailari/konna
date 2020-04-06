package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.ecs.newecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.newecs.World;
import fi.jakojaannos.roguelite.engine.ecs.newecs.sample.EntitySpliterator;

@SuppressWarnings("deprecation")
public class SystemDispatcherImpl implements SystemDispatcher, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(SystemDispatcherImpl.class);
    private static final boolean LOG_SYSTEM_TICK = false;
    private static final boolean LOG_GROUP_TICK = false;
    private static final boolean LOG_TICK = false;


    private final ForkJoinPool threadPool;
    private final List<SystemGroup> systemGroups;

    private int tick = 0;

    public SystemDispatcherImpl(final List<SystemGroup> systemGroups) {
        this.systemGroups = systemGroups;

        this.threadPool = new ForkJoinPool(4,
                                           SystemDispatcherImpl::workerThreadFactory,
                                           null,
                                           false);
    }

    @Override
    public void tick(final World world) {
        final var queue = new HashSet<>(this.systemGroups);

        final Predicate<SystemGroup> isReadyToTick = (group) -> group.isEnabled()
                                                                && group.getDependencies()
                                                                        .stream()
                                                                        .noneMatch(queue::contains);

        if (LOG_TICK) {
            LOG.debug("Tick #{}", this.tick);
        }
        this.tick++;
        final var ticked = new ArrayList<SystemGroup>(queue.size());
        while (queue.stream().anyMatch(isReadyToTick)) {
            queue.stream() // TODO: locks on entity/component storage and make this parallel?
                 .filter(isReadyToTick)
                 .forEach(group -> {
                     ticked.add(group);
                     tick(group, world);
                 });

            ticked.forEach(queue::remove);
            ticked.clear();
        }
    }

    private void tick(final SystemGroup group, final World world) {
        if (LOG_GROUP_TICK) {
            LOG.debug("Ticking group \"{}\"", group.getName());
        }

        // XXX: This has to be sequential by the spec. The system execution order must match the registration order
        group.getSystems()
             .forEach(systemObj -> {
                 if (LOG_SYSTEM_TICK) {
                     LOG.debug("Ticking system \"{}\"", systemObj.getClass().getSimpleName());
                 }
                 if (systemObj instanceof ECSSystem legacySystem) {
                     final var requirements = new ComponentOnlyRequirementsBuilder();
                     legacySystem.declareRequirements(requirements);
                     final var entities = world.getEntityManager()
                                               .getEntitiesWith(requirements.required(),
                                                                requirements.excluded());
                     legacySystem.tick(entities, world);
                 } else if (systemObj instanceof EcsSystem<?, ?, ?> system) {
                     // XXX: This cannot be filter as that would possibly prevent tick in situations where previous system enables the next system
                     if (isEnabled(system)) {
                         dispatch(world, system, this.threadPool);
                     }
                 } else {
                     throw new IllegalStateException("Invalid system type: " + systemObj.getClass().getSimpleName());
                 }
             });
    }

    private boolean isEnabled(final EcsSystem<?, ?, ?> system) {
        return true;
    }

    @Override
    public void close() throws Exception {
        final var exceptions = this.systemGroups.stream()
                                                .flatMap(g -> g.getSystems().stream())
                                                .filter(AutoCloseable.class::isInstance)
                                                .map(s -> {
                                                    try {
                                                        ((AutoCloseable) s).close();
                                                        return null;
                                                    } catch (final Exception e) {
                                                        return e;
                                                    }
                                                })
                                                .filter(Objects::nonNull)
                                                .toArray(Exception[]::new);

        if (exceptions.length > 0) {
            LOG.error("SYSTEM DISPATCHER FAILED TO DISPOSE ONE OR MORE SYSTEM(S)");
            throw new SystemDisposeException(exceptions);
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
        final var entitySpliterator = new EntitySpliterator<>(requirements.entityDataComponentTypes(),
                                                              world,
                                                              requirements::entityDataFactory);

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

    private static ForkJoinWorkerThread workerThreadFactory(final ForkJoinPool pool) {
        final var worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("ecs-worker-" + worker.getPoolIndex());
        return worker;
    }

    private static record ComponentOnlyRequirementsBuilder(
            List<Class<? extends Component>>required,
            List<Class<? extends Component>>excluded
    ) implements RequirementsBuilder {
        public ComponentOnlyRequirementsBuilder() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public RequirementsBuilder withComponent(final Class<? extends Component> componentClass) {
            this.required.add(componentClass);
            return this;
        }

        @Override
        public RequirementsBuilder withoutComponent(final Class<? extends Component> componentClass) {
            this.excluded.add(componentClass);
            return this;
        }

        @Override
        public RequirementsBuilder tickAfter(final Class<? extends ECSSystem> other) {
            return this;
        }

        @Override
        public RequirementsBuilder tickBefore(final Class<? extends ECSSystem> other) {
            return this;
        }

        @Override
        public RequirementsBuilder tickAfter(final fi.jakojaannos.roguelite.engine.ecs.SystemGroup group) {
            return this;
        }

        @Override
        public RequirementsBuilder tickBefore(final fi.jakojaannos.roguelite.engine.ecs.SystemGroup group) {
            return this;
        }

        @Override
        public RequirementsBuilder addToGroup(final fi.jakojaannos.roguelite.engine.ecs.SystemGroup group) {
            return this;
        }

        @Override
        public RequirementsBuilder requireResource(final Class<? extends Resource> resource) {
            return this;
        }

        @Override
        public RequirementsBuilder requireProvidedResource(final Class<? extends ProvidedResource> resource) {
            return this;
        }
    }

    public static class SystemDisposeException extends Exception {
        SystemDisposeException(final Exception[] exceptions) {
            super(Arrays.stream(exceptions)
                        .map(Exception::toString)
                        .reduce(new StringBuilder(),
                                (stringBuilder, s) -> stringBuilder.append(", ").append(s),
                                StringBuilder::append)
                        .toString());
        }
    }
}
