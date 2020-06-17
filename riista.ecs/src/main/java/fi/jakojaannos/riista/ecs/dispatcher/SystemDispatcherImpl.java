package fi.jakojaannos.riista.ecs.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Collectors;

import fi.jakojaannos.riista.ecs.*;
import fi.jakojaannos.riista.ecs.systemdata.ParsedRequirements;
import fi.jakojaannos.riista.ecs.systemdata.SystemInputRecord;

public class SystemDispatcherImpl implements SystemDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SystemDispatcherImpl.class);

    /**
     * Immutable system state with everything which is hinted to be enabled by default is set to enabled.
     */
    private static final SystemState EVERYTHING_ENABLED_BY_DEFAULT_ENABLED = new PermanentDefaultsSystemState();


    private final ForkJoinPool threadPool;
    private final List<SystemGroup> systemGroups;
    private final List<EcsSystem<?, ?, ?>> allSystems;
    // TODO: Move to SystemContext which contains the requirements and enabled status
    //       - possibly move the system there, too, and make groups rely on SystemIds
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends EcsSystem>, ParsedRequirements> systemRequirements;
    private boolean parallel;
    private int tick;

    @Override
    public Collection<Class<?>> getSystems() {
        return this.allSystems.stream()
                              .map(Object::getClass)
                              .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public Collection<SystemGroup> getGroups() {
        return Collections.unmodifiableCollection(this.systemGroups);
    }

    @Override
    public void setParallel(final boolean state) {
        this.parallel = state;
    }

    public SystemDispatcherImpl(final List<SystemGroup> systemGroups) {
        this.parallel = true;

        this.systemGroups = systemGroups;
        this.allSystems = this.systemGroups.stream()
                                           .flatMap(systemGroup -> systemGroup.getSystems().stream())
                                           .collect(Collectors.toUnmodifiableList());

        this.threadPool = new ForkJoinPool(4,
                                           SystemDispatcherImpl::workerThreadFactory,
                                           null,
                                           false);

        this.systemRequirements = this.systemGroups
                .stream()
                .flatMap(group -> group.getSystems().stream())
                .filter(system -> EcsSystem.class.isAssignableFrom(system.getClass()))
                .collect(Collectors.toMap(EcsSystem::getClass, SystemDispatcherImpl::resolveRequirements));
    }

    @Override
    public SystemState createDefaultState() {
        return new SystemStateImpl(this.allSystems, this.systemGroups);
    }

    @Override
    public void tick(final World world, final Collection<Object> systemEvents) {
        tick(world, EVERYTHING_ENABLED_BY_DEFAULT_ENABLED, systemEvents);
    }

    @Override
    public void tick(
            final World world,
            final SystemState systemState,
            final Collection<Object> systemEvents
    ) {
        final var events = constructEventLookup(systemEvents);
        enableEventListeners(systemState, events.keySet());

        final var queue = new HashSet<>(this.systemGroups);

        LOG.debug(LogCategories.DISPATCHER_TICK, "Tick #{} (Dispatcher {})", this.tick, this);
        this.tick++;
        final var ticked = new ArrayList<SystemGroup>(queue.size());

        while (!queue.isEmpty()) {
            // TODO: locks on entity/component storage and make this parallel?
            for (final var group : queue) {
                if (isGroupReadyToTick(group, systemState, queue)) {
                    ticked.add(group);
                    tick(group, world, systemState, events);
                }
            }

            // Break out if none of the systems in queue could be ticked
            if (ticked.size() == 0) {
                break;
            }

            ticked.forEach(queue::remove);
            ticked.clear();
        }
    }

    private void enableEventListeners(
            final SystemState systemState,
            final Set<Class<?>> eventClasses
    ) {
        for (final var systemObj : this.allSystems) {
            if (systemObj instanceof EcsSystem<?, ?, ?> system) {
                final var requirements = requirementsFor(system);

                var shouldEnable = false;
                var shouldDisable = false;
                final var requiredTypes = requirements.events().componentTypes();

                for (int i = 0; i < requiredTypes.length; i++) {
                    // Skip all event types that are not present in the lookup
                    if (!eventClasses.contains(requiredTypes[i])) {
                        continue;
                    }

                    // Event is present, figure out if we need to enable/disable it
                    final var hasEnableOn = requirements.events().enableOn()[i];
                    final var hasDisableOn = requirements.events().disableOn()[i];
                    if (hasDisableOn) {
                        shouldDisable = true;
                    } else if (hasEnableOn) {
                        shouldEnable = true;
                    }
                }

                // Disable takes precedence
                if (shouldDisable) {
                    systemState.setState(system.getClass(), false);
                } else if (shouldEnable) {
                    systemState.setState(system.getClass(), true);
                }
            }
        }
    }

    private void tick(
            final SystemGroup group,
            final World world,
            final SystemState systemState,
            final Map<Class<?>, Collection<Object>> events
    ) {
        LOG.trace(LogCategories.DISPATCHER_GROUP, "Ticking group \"{}\"", group.getName());

        // XXX: This has to be sequential by the spec: (do not use parallel streams etc.)
        //      "The system execution order within the group must match the registration order"
        for (final var system : group.getSystems()) {
            LOG.trace(LogCategories.DISPATCHER_SYSTEM, "Ticking system \"{}\"", system.getClass().getSimpleName());

            // XXX: This cannot be filter as that would possibly prevent tick in situations where previous
            //      system enables the next system
            if (systemState.isEnabled(system.getClass())) {
                dispatch(world, system, this.threadPool, events);
            }
            world.commitEntityModifications();
        }
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
            final ForkJoinPool threadPool,
            final Map<Class<?>, Collection<Object>> events
    ) {
        final var requirements = requirementsFor(system);
        final var systemEvents = requirements.constructEvents(events);

        final Object[] resources = world.fetchResources(requirements.resources().componentTypes());
        final var systemResources = requirements.constructResources(resources);
        final var entityStream = world.iterateEntities(requirements.entityData().componentTypes(),
                                                       requirements.entityData().excluded(),
                                                       requirements.entityData().optional(),
                                                       requirements::constructEntityData,
                                                       this.parallel);

        // Return if event constraints were not met
        if (systemEvents == null) {
            return;
        }

        if (this.parallel) {
            try {
                threadPool.submit(
                        () -> system.tick(systemResources, entityStream, systemEvents)
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
        } else {
            system.tick(systemResources, entityStream, systemEvents);
        }
    }

    @Override
    public void close() {
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

    @SuppressWarnings("unchecked")
    private static <TResources, TEntityData, TEvents> ParsedRequirements<TResources, TEntityData, TEvents>
    resolveRequirements(final EcsSystem<TResources, TEntityData, TEvents> system) {
        final var interfaceType = Arrays.stream(system.getClass().getGenericInterfaces())
                                        // Filter out any non-parameterized (non-generic) interfaces
                                        .filter(type -> ParameterizedType.class.isAssignableFrom(type.getClass()))
                                        // Convert to a stream of parameterized types
                                        .map(ParameterizedType.class::cast)
                                        // Find EcsSystem from those interfaces
                                        // (Find EcsSystem from the implements list of the system)
                                        .filter(SystemDispatcherImpl::isEcsSystem)
                                        .findFirst()
                                        .orElseThrow(() -> new IllegalStateException(
                                                "Could not find EcsSystem from system implements list!"));

        // Now, we have parameterized type we can pull the type arguments from
        final var resourceDataType = interfaceType.getActualTypeArguments()[0];
        final var entityDataType = interfaceType.getActualTypeArguments()[1];
        final var eventDataType = interfaceType.getActualTypeArguments()[2];

        // ...and then just construct the requirements from them
        return new ParsedRequirements<>(system.getClass().getSimpleName(),
                                        SystemInputRecord.Resources.createFor((Class<TResources>) resourceDataType),
                                        SystemInputRecord.EntityData.createFor((Class<TEntityData>) entityDataType),
                                        SystemInputRecord.Events.createFor((Class<TEvents>) eventDataType));
    }

    private static boolean isEcsSystem(final ParameterizedType type) {
        return ((Class<?>) type.getRawType()).isAssignableFrom(EcsSystem.class);
    }

    private static Map<Class<?>, Collection<Object>> constructEventLookup(final Collection<Object> eventList) {
        final var lookup = new HashMap<Class<?>, Collection<Object>>();
        eventList.forEach(event -> lookup.computeIfAbsent(event.getClass(), key -> new ArrayList<>())
                                         .add(event));
        return lookup;
    }

    /**
     * A group is ready to tick if all of the following are satisfied.
     * <ol>
     *     <li>The group itself is enabled</li>
     *     <li>All its dependencies have been ticked (none of them are queued for tick anymore)</li>
     *     <li>Any of its systems is enabled</li>
     * </ol>
     */
    private static boolean isGroupReadyToTick(
            final SystemGroup group,
            final SystemState systemState,
            final Set<SystemGroup> queue
    ) {
        return systemState.isEnabled(group)
               && group.getDependencies()
                       .stream()
                       .noneMatch(queue::contains)
               && group.getSystems()
                       .stream()
                       .map(Object::getClass)
                       .anyMatch(systemState::isEnabled);
    }

    private static ForkJoinWorkerThread workerThreadFactory(final ForkJoinPool pool) {
        final var worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
        worker.setName("ecs-worker-" + worker.getPoolIndex());
        return worker;
    }

    public static class SystemDisposeException extends RuntimeException {
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
