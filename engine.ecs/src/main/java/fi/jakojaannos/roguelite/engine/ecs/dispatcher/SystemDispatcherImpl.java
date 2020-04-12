package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.ParsedRequirements;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.RequirementsBuilder;

public class SystemDispatcherImpl implements SystemDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SystemDispatcherImpl.class);

    /**
     * Immutable system state with everything which is hinted to be enabled by default is set to enabled.
     */
    private static final SystemState EVERYTHING_ENABLED_BY_DEFAULT_ENABLED = new SystemState() {
        @Override
        public boolean isEnabled(final Class<?> systemClass) {
            return !systemClass.isAnnotationPresent(DisabledByDefault.class);
        }

        @Override
        public boolean isEnabled(final SystemGroup systemGroup) {
            return systemGroup.isEnabledByDefault();
        }

        @Override
        public void setState(final Class<?> systemClass, final boolean state) {
        }

        @Override
        public void setState(final SystemGroup systemGroup, final boolean state) {
        }
    };


    private final ForkJoinPool threadPool;
    private final List<SystemGroup> systemGroups;
    private final List<Object> allSystems;
    // TODO: Move to SystemContext which contains the requirements and enabled status
    //       - possibly move the system there, too, and make groups rely on SystemIds
    @SuppressWarnings("rawtypes")
    private final Map<Class<? extends EcsSystem>, ParsedRequirements> systemRequirements = new ConcurrentHashMap<>();
    private boolean parallel;
    private int tick;

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

        this.systemGroups.stream()
                         .flatMap(group -> group.getSystems().stream())
                         .filter(system -> EcsSystem.class.isAssignableFrom(system.getClass()))
                         .map(EcsSystem.class::cast)
                         .forEach(this::resolveRequirements);
    }

    @Override
    public void setParallel(final boolean state) {
        this.parallel = state;
    }

    private <TResources, TEntityData, TEvents> void resolveRequirements(final EcsSystem<TResources, TEntityData, TEvents> system) {
        final var requirementsBuilder = new RequirementsBuilder<TResources, TEntityData, TEvents>();
        system.declareRequirements(requirementsBuilder);
        this.systemRequirements.put(system.getClass(), requirementsBuilder.build());
    }

    @Override
    public SystemState createDefaultState() {
        return new SystemStateImpl(this.allSystems, this.systemGroups);
    }

    @Override
    public void tick(final World world) {
        tick(world, EVERYTHING_ENABLED_BY_DEFAULT_ENABLED, List.of());
    }

    @Override
    public void tick(
            final World world,
            final SystemState systemState,
            final Collection<Object> systemEvents
    ) {
        final var events = constructEventLookup(systemEvents);
        enableEventListeners(systemState, events);

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
            final Map<Class<?>, Object> events
    ) {
        for (final var systemObj : this.allSystems) {
            if (systemObj instanceof EcsSystem<?, ?, ?> system) {
                final var requirements = requirementsFor(system);

                var shouldEnable = false;
                var shouldDisable = false;
                final var requiredTypes = requirements.events().componentTypes();

                for (int i = 0; i < requiredTypes.length; i++) {
                    // Skip all event types that are not present in the lookup
                    if (!events.containsKey(requiredTypes[i])) {
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
            final Map<Class<?>, Object> events
    ) {
        LOG.trace(LogCategories.DISPATCHER_GROUP, "Ticking group \"{}\"", group.getName());

        // XXX: This has to be sequential by the spec: (do not use parallel streams etc.)
        //      "The system execution order within the group must match the registration order"
        for (final var systemObj : group.getSystems()) {
            LOG.trace(LogCategories.DISPATCHER_SYSTEM, "Ticking system \"{}\"", systemObj.getClass().getSimpleName());
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
                if (systemState.isEnabled(system.getClass())) {
                    dispatch(world, system, this.threadPool, events);
                }
            } else {
                throw new IllegalStateException("Invalid system type: " + systemObj.getClass().getSimpleName());
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
            final Map<Class<?>, Object> events
    ) {
        final var requirements = requirementsFor(system);
        final var systemEvents = requirements.constructEvents(events);

        final Object[] resources = world.fetchResources(requirements.resources().componentTypes());
        final var systemResources = requirements.constructResources(resources);
        final var entitySpliterator = world.iterateEntities(requirements.entityData().componentTypes(),
                                                            requirements.entityData().excluded(),
                                                            requirements::constructEntityData);

        // Return if event constraints were not met
        if (systemEvents == null) {
            return;
        }

        try {
            threadPool.submit(
                    () -> system.tick(systemResources,
                                      StreamSupport.stream(entitySpliterator, this.parallel),
                                      systemEvents)
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

    private static Map<Class<?>, Object> constructEventLookup(final Collection<Object> eventList) {
        return eventList.stream()
                        .collect(Collectors.toMap(Object::getClass, event -> event));
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

    @Deprecated
    private static record ComponentOnlyRequirementsBuilder(
            List<Class<?>>required,
            List<Class<?>>excluded
    ) implements fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder {

        public ComponentOnlyRequirementsBuilder() {
            this(new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder withComponent(final Class<?> componentClass) {
            this.required.add(componentClass);
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder withoutComponent(final Class<?> componentClass) {
            this.excluded.add(componentClass);
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder tickAfter(final Class<? extends ECSSystem> ignored) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder tickBefore(final Class<? extends ECSSystem> ignored) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder addToGroup(final Object ignored) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder requireResource(final Class<?> ignored) {
            return this;
        }

        @Override
        public fi.jakojaannos.roguelite.engine.ecs.legacy.RequirementsBuilder requireProvidedResource(final Class<?> ignored) {
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
