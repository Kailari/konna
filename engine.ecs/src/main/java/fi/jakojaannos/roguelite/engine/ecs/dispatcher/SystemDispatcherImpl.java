package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.World;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Resource;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.ParsedRequirements;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.RequirementsBuilder;

@SuppressWarnings("deprecation")
public class SystemDispatcherImpl implements SystemDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SystemDispatcherImpl.class);
    private static final boolean LOG_SYSTEM_TICK = false;
    private static final boolean LOG_GROUP_TICK = false;
    private static final boolean LOG_TICK = false;


    private final ForkJoinPool threadPool;
    private final List<SystemGroup> systemGroups;

    // TODO: Move to SystemContext which contains the requirements and enabled status
    //       - possibly move the system there, too, and make groups rely on SystemIds
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends EcsSystem>, ParsedRequirements> systemRequirements = new ConcurrentHashMap<>();

    private int tick;

    public SystemDispatcherImpl(final List<SystemGroup> systemGroups) {
        this.systemGroups = systemGroups;
        this.systemGroups.stream()
                         .flatMap(group -> group.getSystems().stream())
                         .filter(system -> EcsSystem.class.isAssignableFrom(system.getClass()))
                         .map(EcsSystem.class::cast)
                         .forEach(this::resolveRequirements);

        this.threadPool = new ForkJoinPool(4,
                                           SystemDispatcherImpl::workerThreadFactory,
                                           null,
                                           false);
    }

    private <TResources, TEntityData, TEvents> void resolveRequirements(final EcsSystem<TResources, TEntityData, TEvents> system) {
        final var requirementsBuilder = new RequirementsBuilder<TResources, TEntityData, TEvents>();
        system.declareRequirements(requirementsBuilder);
        this.systemRequirements.put(system.getClass(), requirementsBuilder.build());
    }

    @Override
    public void tick(final World world) {
        final var queue = new HashSet<>(this.systemGroups);

        final Predicate<SystemGroup> isReadyToTick = (group) -> group.isEnabled()
                                                                && group.getDependencies()
                                                                        .stream()
                                                                        .noneMatch(queue::contains);

        if (LOG_TICK) {
            LOG.debug("Tick #{} (Dispatcher {})", this.tick, this);
        }
        this.tick++;
        final var ticked = new ArrayList<fi.jakojaannos.roguelite.engine.ecs.SystemGroup>(queue.size());
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

        // XXX: This has to be sequential by the spec. "The system execution order must match the registration order"
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
                     // XXX: This cannot be filter as that would possibly prevent tick in situations where previous
                     //      system enables the next system
                     if (isEnabled(system)) {
                         dispatch(world, system, this.threadPool);
                     }
                 } else {
                     throw new IllegalStateException("Invalid system type: " + systemObj.getClass().getSimpleName());
                 }
                 world.commitEntityModifications();
             });
    }

    private boolean isEnabled(final EcsSystem<?, ?, ?> system) {
        return true;
    }

    @SuppressWarnings("unchecked")
    private <TEvents, TEntityData, TResources> ParsedRequirements<TResources, TEntityData, TEvents> requirementsFor(
            final EcsSystem<TResources, TEntityData, TEvents> system
    ) {
        return this.systemRequirements.get(system.getClass());
    }

    private <TResources, TEntityData, TEvents> void dispatch(
            final World world,
            final EcsSystem<TResources, TEntityData, TEvents> system,
            final ForkJoinPool threadPool
    ) {
        final var requirements = requirementsFor(system);
        final var systemResources = requirements.constructResources(world.getResources());
        final var entitySpliterator = new EntitySpliterator<>(requirements.entityData().componentTypes(),
                                                              requirements.entityData().excluded(),
                                                              world,
                                                              requirements::constructEntityData);

        try {
            threadPool.submit(
                    () -> system.tick(systemResources,
                                      StreamSupport.stream(entitySpliterator, false),
                                      null)
            ).get();
        } catch (final InterruptedException e) {
            LOG.warn("System \"" + system.getClass().getSimpleName() + "\" was interrupted!");
        } catch (final ExecutionException ee) {
            LOG.error("System \"" + system.getClass().getSimpleName() + "\" failure!", ee);
            if (ee.getCause() instanceof RuntimeException e) {
                throw e;
            } else {
                throw new RuntimeException("Assuming unrecoverable error, dispatcher is going crashing down!", ee);
            }
        }
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

    private static ForkJoinWorkerThread workerThreadFactory(final ForkJoinPool pool) {
        final var worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("ecs-worker-" + worker.getPoolIndex());
        return worker;
    }

    private static record ComponentOnlyRequirementsBuilder(
            List<Class<? extends Component>>required,
            List<Class<? extends Component>>excluded
    ) implements fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder {

        public ComponentOnlyRequirementsBuilder() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder withComponent(final Class<? extends Component> componentClass) {
            this.required.add(componentClass);
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder withoutComponent(final Class<? extends Component> componentClass) {
            this.excluded.add(componentClass);
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder tickAfter(final Class<? extends ECSSystem> other) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder tickBefore(final Class<? extends ECSSystem> other) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder tickAfter(final fi.jakojaannos.roguelite.engine.ecs.legacy.SystemGroup group) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder tickBefore(final fi.jakojaannos.roguelite.engine.ecs.legacy.SystemGroup group) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder addToGroup(final fi.jakojaannos.roguelite.engine.ecs.legacy.SystemGroup group) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder requireResource(final Class<?> resource) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder requireProvidedResource(final Class<?> resource) {
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
