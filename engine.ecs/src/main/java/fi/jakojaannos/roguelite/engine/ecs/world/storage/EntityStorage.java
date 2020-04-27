package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;

public class EntityStorage {
    private final Collection<Archetype> archetypes = new ArrayList<>();

    public <TEntityData> Stream<EntityDataHandle<TEntityData>> stream(
            final Class<?>[] componentClasses,
            final boolean[] excluded,
            final boolean[] optional,
            final Function<Object[], TEntityData> dataFactory
    ) {
        // Component classes without excluded components
        final var requiredClasses = IntStream.range(0, componentClasses.length)
                                             .filter(i -> !excluded[i])
                                             .mapToObj(i -> componentClasses[i])
                                             .toArray(Class[]::new);

        // Filter by exclusion and flatten per-archetype streams to a single stream
        return this.archetypes.parallelStream()
                              .filter(archetype -> archetype.matchesRequirements(componentClasses,
                                                                                 excluded,
                                                                                 optional))
                              .flatMap(archetype -> archetype.stream(requiredClasses,
                                                                     optional,
                                                                     dataFactory));
    }
}
