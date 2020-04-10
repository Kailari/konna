package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.SystemGroup;
import fi.jakojaannos.roguelite.engine.ecs.SystemState;
import fi.jakojaannos.roguelite.engine.ecs.annotation.DisabledByDefault;

public class SystemStateImpl implements SystemState {
    private final Map<Object, Boolean> enabled;

    public SystemStateImpl(
            final Iterable<Object> systems,
            final Iterable<SystemGroup> systemGroups
    ) {
        this.enabled = StreamSupport.stream(systems.spliterator(), false)
                                    .collect(Collectors.toMap(system -> system, this::isEnabledByDefault));
        systemGroups.forEach(group -> this.enabled.put(group, true));
    }

    private boolean isEnabledByDefault(final Object system) {
        return !system.getClass().isAnnotationPresent(DisabledByDefault.class);
    }

    @Override
    public boolean isEnabled(final Object system) {
        final var enabled = this.enabled.get(system);
        return enabled != null && enabled;
    }

    @Override
    public boolean isEnabled(final SystemGroup systemGroup) {
        final var enabled = this.enabled.get(systemGroup);
        return enabled != null && enabled;
    }

    @Override
    public void setState(final Object system, final boolean state) {
        this.enabled.put(system, state);
    }

    @Override
    public void setState(final SystemGroup systemGroup, final boolean state) {
        this.enabled.put(systemGroup, state);
    }
}
