package fi.jakojaannos.roguelite.engine.ecs.systems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemDispatcher;
import fi.jakojaannos.roguelite.engine.ecs.World;

/**
 * Default {@link SystemDispatcher} implementation.
 */
public class SystemDispatcherImpl implements SystemDispatcher {
    private static final Logger LOG = LoggerFactory.getLogger(SystemDispatcherImpl.class);

    private final SystemStorage systems;

    public SystemDispatcherImpl(final SystemStorage systems) {
        this.systems = systems;
    }

    @Override
    public void dispatch(final World world) {
        final var dispatchContext = new DispatchContext(this.systems.getSystems(), this.systems.getSystemGroups());

        Optional<Class<? extends ECSSystem>> nextEntryPoint;
        while ((nextEntryPoint = dispatchContext.findAnyNotDispatched()).isPresent()) {
            final Deque<SystemContext> queue = new ArrayDeque<>();
            queue.add(this.systems.findContextByType(nextEntryPoint.get()));
            while (!queue.isEmpty()) {
                final var systemContext = queue.getFirst();

                // Due to the fact our dependency walker is paranoid, same systems may end up on the
                // queue multiple times. If that happens, just throw away systems already dispatched.
                if (dispatchContext.isDispatched(systemContext.getInstance().getClass())) {
                    queue.remove();
                    continue;
                }

                if (dispatchContext.isReadyToDispatch(systemContext)) {
                    removeFromQueueAndDispatch(world, dispatchContext, queue, systemContext);
                } else {
                    queueDependenciesWithoutRemovingFromQueue(dispatchContext, queue, systemContext);
                }
            }
        }
    }

    private void removeFromQueueAndDispatch(
            final World world,
            final DispatchContext dispatchContext,
            final Deque<SystemContext> queue,
            final SystemContext systemContext
    ) {
        queue.removeFirst();
        systemContext.getInstance()
                     .tick(world.getEntityManager()
                                .getEntitiesWith(systemContext.getRequirements().requiredComponents(),
                                                 systemContext.getRequirements().excludedComponents(),
                                                 systemContext.getRequirements().requiredGroups(),
                                                 systemContext.getRequirements().excludedGroups()),
                           world);
        dispatchContext.setDispatched(systemContext);
    }

    private void queueDependenciesWithoutRemovingFromQueue(
            final DispatchContext dispatchContext,
            final Deque<SystemContext> queue,
            final SystemContext systemContext
    ) {
        // TODO: This extensive dependency tree walking here should NOT be necessary. Simplify
        //  things for easier handling of transitive dependencies caused by groups
        //  -   One possible solution is to bake the groups into the dependency graph at build() by
        //      adding the group dependencies to the systems.
        //  -   Another one is to add a getAllDependencies() -method to SystemDependencies for
        //      getting all concrete systems the system requires to be dispatched before it can run
        //      (basically the spaghetti below, but inside SystemDependencies)

        systemContext.getDependencies()
                     .stream()
                     .filter(dispatchContext::notDispatched)
                     .map(this.systems::findContextByType)
                     .forEach(queue::addFirst);

        systemContext.getDependencies()
                     .groupDependenciesAsStream()
                     .filter(dispatchContext::notDispatched)
                     .map(this.systems::findGroupByType)
                     .flatMap(InternalSystemGroup::getSystems)
                     .filter(dispatchContext::notDispatched)
                     .map(this.systems::findContextByType)
                     .forEach(queue::addFirst);

        systemContext.getGroups()
                     .filter(dispatchContext::notDispatched)
                     .map(this.systems::findGroupByType)
                     .flatMap(InternalSystemGroup::getSystems)
                     .filter(dispatchContext::notDispatched)
                     .filter(systemClass -> !systemClass.equals(systemContext.getInstance().getClass()))
                     .map(this.systems::findContextByType)
                     .forEach(queue::addFirst);

        systemContext.getGroups()
                     .filter(dispatchContext::notDispatched)
                     .map(this.systems::findGroupByType)
                     .flatMap(InternalSystemGroup::getDependencies)
                     .filter(dispatchContext::notDispatched)
                     .filter(systemClass -> !systemClass.equals(systemContext.getInstance().getClass()))
                     .map(this.systems::findContextByType)
                     .forEach(queue::addFirst);

        systemContext.getGroups()
                     .filter(dispatchContext::notDispatched)
                     .map(this.systems::findGroupByType)
                     .flatMap(InternalSystemGroup::getGroupDependencies)
                     .filter(dispatchContext::notDispatched)
                     .map(this.systems::findGroupByType)
                     .flatMap(InternalSystemGroup::getSystems)
                     .filter(dispatchContext::notDispatched)
                     .filter(systemClass -> !systemClass.equals(systemContext.getInstance().getClass()))
                     .map(this.systems::findContextByType)
                     .forEach(queue::addFirst);
    }

    @Override
    public void close() throws Exception {
        final var exceptions = this.systems.nonPrioritizedStream()
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

    private static class SystemDisposeException extends Exception {
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
