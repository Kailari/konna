package fi.jakojaannos.roguelite.engine.ecs.systems;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Internal representation of status of a dispatch. Stores current dispatch status for systems and groups. Used to
 * determine if system has been ticked during current dispatch.
 */
public class DispatchContext {
    private final Map<SystemGroup, InternalSystemGroup> groups = new HashMap<>();
    private final Map<Class<? extends ECSSystem>, Boolean> dispatchStatus = new HashMap<>();

    public DispatchContext(
            final Stream<SystemContext> systemContexts,
            final Stream<InternalSystemGroup> systemGroups
    ) {
        systemContexts.forEach(ctx -> this.dispatchStatus.put(ctx.getInstance().getClass(), false));
        systemGroups.forEach(internalGroup -> this.groups.put(internalGroup.getGroup(), internalGroup));
    }

    public boolean isReadyToDispatch(final SystemContext systemContext) {
        return systemContext.getDependencies()
                            .stream()
                            .allMatch(this::isDispatched)
                && systemContext.getDependencies()
                                .groupDependenciesAsStream()
                                .allMatch(this::isDispatched)
                && systemContext.getGroups()
                                .allMatch(this::isReadyToDispatch);
    }

    public boolean isReadyToDispatch(final SystemGroup group) {
        final var internalGroup = this.groups.get(group);
        return internalGroup.getDependencies()
                            .allMatch(this::isDispatched)
                && internalGroup.getGroupDependencies()
                                .allMatch(this::isDispatched);
    }

    public boolean notDispatched(final Class<? extends ECSSystem> system) {
        return !isDispatched(system);
    }

    public boolean notDispatched(final SystemGroup group) {
        return !isDispatched(group);
    }

    public boolean isDispatched(final Class<? extends ECSSystem> system) {
        return this.dispatchStatus.get(system);
    }

    public boolean isDispatched(final SystemGroup group) {
        return this.groups.get(group)
                          .getSystems()
                          .allMatch(this::isDispatched);
    }

    public Optional<Class<? extends ECSSystem>> findAnyNotDispatched() {
        for (final var entry : this.dispatchStatus.entrySet()) {
            if (!entry.getValue()) {
                return Optional.of(entry.getKey());
            }
        }

        return Optional.empty();
    }

    public void setDispatched(final SystemContext system) {
        this.dispatchStatus.put(system.getInstance().getClass(), true);
    }
}
