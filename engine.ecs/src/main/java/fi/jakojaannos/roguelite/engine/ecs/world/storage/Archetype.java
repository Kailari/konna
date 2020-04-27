package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;

/**
 * Archetype is a collection of entities with certain specific collection of components. Each entity belonging to an
 * archetype is guaranteed to have the exact same set of components.
 * <p>
 * This implies, too, that adding/removing components also means changing the archetype of the said entity, making it a
 * relatively expensive operation (copy from archetype to another, possible element swaps to keep storage contiguous).
 */
public class Archetype {
    private final Class<?>[] componentClasses;

    private EntityChunk root;

    public Archetype(final Class<?>[] componentClasses) {
        this.componentClasses = componentClasses;
    }

    public boolean matchesRequirements(
            final Class<?>[] componentClasses,
            final boolean[] excluded,
            final boolean[] optional
    ) {
        return false;
    }

    public <TEntityData> Stream<EntityDataHandle<TEntityData>> stream(
            final Class<?>[] componentClasses,
            final boolean[] optional,
            final Function<Object[], TEntityData> dataFactory
    ) {
        return StreamSupport.stream(new EntityChunkSpliterator<>(this.root,
                                                                 componentClasses,
                                                                 optional,
                                                                 dataFactory),
                                    true);
    }
}
