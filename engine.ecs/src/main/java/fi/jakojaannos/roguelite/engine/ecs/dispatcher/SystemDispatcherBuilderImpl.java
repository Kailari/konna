package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

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
