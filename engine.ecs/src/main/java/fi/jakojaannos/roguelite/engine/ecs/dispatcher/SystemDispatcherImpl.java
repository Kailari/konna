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
import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;
import fi.jakojaannos.roguelite.engine.ecs.legacy.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.ParsedRequirements;
import fi.jakojaannos.roguelite.engine.ecs.systemdata.RequirementsBuilder;

@SuppressWarnings("deprecation")
public class SystemDispatcherImpl implements SystemDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SystemDispatcherImpl.class);
    private static final boolean LOG_SYSTEM_TICK = false;
    private static final boolean LOG_GROUP_TICK = false;
    private static final boolean LOG_TICK = false;

    private static final SystemState ALL_SYSTEMS_ENABLED = new SystemState() {
        @Override
        public boolean isEnabled(final Object system) {
            return true;
        }

        @Override
        public boolean isEnabled(final SystemGroup systemGroup) {
            return true;
        }

        @Override
        public void setState(final Object system, final boolean state) {
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

    private int tick;

    public SystemDispatcherImpl(final List<SystemGroup> systemGroups) {
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
        tick(world, ALL_SYSTEMS_ENABLED, List.of());
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

        if (LOG_TICK) {
            LOG.debug("Tick #{} (Dispatcher {})", this.tick, this);
        }
        this.tick++;
        final var ticked = new ArrayList<SystemGroup>(queue.size());

        // FIXME: isGroupReadyToTick is evaluated twice. Split this to two steps to avoid iterating twice every time
        //          (this impl is O(n^2) overall, collecting could drop it close to O(n) in best case and would likely
        //           not have adverse effect on performance anyway.)
        while (queue.stream().anyMatch(group -> isGroupReadyToTick(group, systemState, queue))) {
            queue.stream() // TODO: locks on entity/component storage and make this parallel?
                 .filter(group -> isGroupReadyToTick(group, systemState, queue))
                 .forEach(group -> {
                     ticked.add(group);
                     tick(group, world, systemState, events);
                 });

            ticked.forEach(queue::remove);
            ticked.clear();
        }

        disableTickOnceListeners(systemState, events);
    }

    private void disableTickOnceListeners(
            final SystemState systemState,
            final Map<Class<?>, Object> events
    ) {
        for (final var systemObj : this.allSystems) {
            if (systemObj instanceof EcsSystem<?, ?, ?> system) {
                final var requirements = requirementsFor(system);

                for (final Class<?> eventClass : events.keySet()) {
                    final var hasEnableOn = Arrays.asList(requirements.events().enableOn())
                                                  .contains(eventClass);
                    final var hasDisableOn = Arrays.asList(requirements.events().disableOn())
                                                   .contains(eventClass);

                    // @EnableOn + @DisableOn = "@TickOnce"
                    if (hasEnableOn && hasDisableOn) {
                        systemState.setState(system, false);
                        break;
                    }
                }
            }
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
                for (final Class<?> eventClass : events.keySet()) {
                    final var hasEnableOn = Arrays.stream(requirements.events().enableOn())
                                                  .anyMatch(clazz -> clazz.isAssignableFrom(eventClass));
                    final var hasDisableOn = Arrays.stream(requirements.events().disableOn())
                                                   .anyMatch(clazz -> clazz.isAssignableFrom(eventClass));

                    // Special case: @EnableOn + @DisableOn = "@TickOnce"
                    // TODO: Events live only for one tick, @TickOnce is implied from no annotations!
                    if (hasEnableOn && hasDisableOn) {
                        shouldEnable = true;
                    } else if (hasDisableOn) {
                        // @DisableOn takes precedence. If any event is @DisableOn, the system will
                        // be disabled, ignoring all other events.
                        shouldDisable = true;
                        break;
                    } else if (hasEnableOn) {
                        // Do not break here as some other event could have @DisableOn, which could
                        // override shouldEnable
                        shouldEnable = true;
                    }
                }

                if (shouldDisable) {
                    systemState.setState(system, false);
                } else if (shouldEnable) {
                    systemState.setState(system, true);
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
                     if (systemState.isEnabled(system)) {
                         dispatch(world, system, this.threadPool, events);
                     }
                 } else {
                     throw new IllegalStateException("Invalid system type: " + systemObj.getClass().getSimpleName());
                 }
                 world.commitEntityModifications();
             });
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
        final var systemResources = requirements.constructResources(world.getResources());
        final var entitySpliterator = new EntitySpliterator<>(requirements.entityData().componentTypes(),
                                                              requirements.entityData().excluded(),
                                                              world,
                                                              requirements::constructEntityData);

        try {
            threadPool.submit(
                    () -> system.tick(systemResources,
                                      StreamSupport.stream(entitySpliterator, true),
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
                       .anyMatch(systemState::isEnabled);
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
