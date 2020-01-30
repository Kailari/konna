package fi.jakojaannos.roguelite.engine.ecs.systems;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.ECSSystem;
import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;

/**
 * Internal {@link ECSSystem} storage for {@link SystemDispatcherImpl}. Provides convenient access to the per-system
 * context with either the system class or a raw instance.
 */
class SystemStorage {
    private final List<SystemContext> systems;
    private final List<InternalSystemGroup> systemGroups;
    private final Map<Class<? extends ECSSystem>, SystemContext> contextLookup;
    private final Map<SystemGroup, InternalSystemGroup> groupLookup;

    SystemStorage(
            final List<SystemContext> systems,
            final List<InternalSystemGroup> systemGroups
    ) {
        this.systems = systems;
        this.systemGroups = systemGroups;
        this.contextLookup = new HashMap<>();
        this.groupLookup = new HashMap<>();
        this.systems.forEach(ctx -> this.contextLookup.put(ctx.getInstance().getClass(),
                                                           ctx));
        this.systemGroups.forEach(group -> this.groupLookup.put(group.getGroup(), group));
    }

    Stream<ECSSystem> nonPrioritizedStream() {
        return this.systems.stream().map(SystemContext::getInstance);
    }

    SystemContext findContextByType(final Class<? extends ECSSystem> systemType) {
        return this.contextLookup.get(systemType);
    }

    Stream<SystemContext> getSystems() {
        return this.systems.stream();
    }

    Stream<InternalSystemGroup> getSystemGroups() {
        return this.systemGroups.stream();
    }

    InternalSystemGroup findGroupByType(final SystemGroup group) {
        return this.groupLookup.get(group);
    }
}
