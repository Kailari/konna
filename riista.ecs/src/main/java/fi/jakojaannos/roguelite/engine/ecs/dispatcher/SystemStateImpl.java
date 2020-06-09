package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.SystemState;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault;

public class SystemStateImpl implements SystemState {
    private final Map<Class<?>, Boolean> enabled;
    private final Map<SystemGroup, Boolean> groups;

    @Override
    public void resetToDefaultState(final Collection<Class<?>> systems) {
        for (final var systemClass : systems) {
            this.enabled.put(systemClass, isEnabledByDefault(systemClass));
        }
    }

    @Override
    public void resetGroupsToDefaultState(final Collection<SystemGroup> systemGroups) {
        for (final var group : systemGroups) {
            this.groups.put(group, group.isEnabledByDefault());
        }
    }

    public SystemStateImpl(
            final Iterable<Object> systems,
            final Iterable<SystemGroup> systemGroups
    ) {
        this.enabled = StreamSupport.stream(systems.spliterator(), false)
                                    .collect(Collectors.toMap(Object::getClass, system -> isEnabledByDefault(system.getClass())));
        this.groups = StreamSupport.stream(systemGroups.spliterator(), false)
                                   .collect(Collectors.toMap(group -> group, SystemGroup::isEnabledByDefault));
    }

    @Override
    public boolean isEnabled(final Class<?> systemClass) {
        final var enabled = this.enabled.get(systemClass);
        return enabled != null && enabled;
    }

    @Override
    public boolean isEnabled(final SystemGroup systemGroup) {
        final var enabled = this.groups.get(systemGroup);
        return enabled != null && enabled;
    }

    @Override
    public void setState(final Class<?> systemClass, final boolean state) {
        this.enabled.put(systemClass, state);
    }

    @Override
    public void setState(final SystemGroup systemGroup, final boolean state) {
        this.groups.put(systemGroup, state);
    }

    private static boolean isEnabledByDefault(final Class<?> systemClass) {
        return !systemClass.isAnnotationPresent(DisabledByDefault.class);
    }
}
