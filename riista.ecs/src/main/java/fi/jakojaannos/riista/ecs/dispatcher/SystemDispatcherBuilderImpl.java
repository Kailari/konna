package fi.jakojaannos.riista.ecs.dispatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

import fi.jakojaannos.riista.ecs.SystemDispatcher;
import fi.jakojaannos.riista.ecs.SystemGroup;

public class SystemDispatcherBuilderImpl implements SystemDispatcher.Builder {
    private final Collection<SystemGroupImpl.Builder> groups = new ArrayList<>();

    @Override
    public SystemDispatcher build() {
        return new SystemDispatcherImpl(this.groups.stream()
                                                   .map(SystemGroupImpl.Builder::asGroup)
                                                   .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public SystemGroup.Builder group(final String name) {
        final var groupBuilder = new SystemGroupImpl.Builder(name, this.groups.size());
        this.groups.add(groupBuilder);

        return groupBuilder;
    }
}
