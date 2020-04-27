package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.world.EntityHandleImpl;

/**
 * Archetype is a collection of entities with certain specific collection of components. Each entity belonging to an
 * archetype is guaranteed to have the exact same set of components.
 * <p>
 * This implies, too, that adding/removing components also means changing the archetype of the said entity, making it a
 * relatively expensive operation (copy from archetype to another, possible element swaps to keep storage contiguous).
 */
public class Archetype {
    private final Class<?>[] componentClasses;
    private final Collection<Removed> removed = new ArrayList<>();

    private final EntityChunk root;
    private EntityChunk head;

    public Class<?>[] getComponentClasses() {
        return this.componentClasses;
    }

    public int getComponentCount() {
        return this.componentClasses.length;
    }

    public Archetype(final Class<?>[] componentClasses) {
        this.componentClasses = componentClasses;
        this.root = new EntityChunk(this);

        this.head = this.root;
    }

    public boolean hasAll(final Class<?>[] componentClasses) {
        return Arrays.asList(this.componentClasses)
                     .containsAll(Arrays.asList(componentClasses));
    }

    public boolean matchesRequirements(
            final Class<?>[] componentClasses,
            final boolean[] excluded,
            final boolean[] optional
    ) {
        for (int i = 0; i < componentClasses.length; i++) {
            var noneMatches = true;
            for (final var componentClass : this.componentClasses) {


                final var other = componentClasses[i];
                if (componentClass.equals(other)) {
                    // Return false if any excluded component class is found
                    if (excluded[i]) {
                        return false;
                    }

                    noneMatches = false;
                }
            }

            // Return false if any of the non-optional, non-excluded component classes are not found
            if (noneMatches && !optional[i] && !excluded[i]) {
                return false;
            }
        }

        // Everything required was found, return true
        return true;
    }

    public <TEntityData> Stream<EntityDataHandle<TEntityData>> stream(
            final Class<?>[] componentClasses,
            final boolean[] optional,
            final Function<Object[], TEntityData> dataFactory,
            final boolean parallel
    ) {
        return StreamSupport.stream(new EntityChunkSpliterator<>(this.root,
                                                                 componentClasses,
                                                                 optional,
                                                                 dataFactory),
                                    parallel);
    }

    public <TComponent> boolean hasComponent(final Class<TComponent> componentClass) {
        return Arrays.asList(this.componentClasses)
                     .contains(componentClass);
    }

    public synchronized void addEntity(
            final EntityHandleImpl entityHandle,
            final Class<?>[] componentClasses,
            final Object[] components
    ) {
        this.head.addEntity(entityHandle, componentClasses, components);

        // Move chain head if new chunk was added
        if (this.head.hasNext()) {
            this.head = this.head.getNext();
        }
    }

    public void commitModifications() {
        var chunk = this.root;
        do {
            chunk.commitAdditions();
            chunk = chunk.getNext();
        } while (chunk != null);

        //for (final var removeTask : this.removed) {
        //    final var entity = removeTask.chunk.getEntity(removeTask.storageIndex);
        //    this.head.moveLastInto(removeTask.chunk, removeTask.storageIndex);
//
        //    if (removeTask.destroyed) {
        //        entity.markDestroyed();
        //    }
        //}
        this.removed.clear();
    }

    /**
     * Queues the removal of an entity in the specified chunk at the given storage index. Note that this does not
     * necessarily mean the entity gets destroyed, but might be due to entity moving to another archetype, too.
     *
     * @param chunk        chunk to remove from. Must be chunk within the chunk chain in this archetype
     * @param storageIndex storage index within the specified chunk
     * @param destroy      should the entity be marked as destroyed after the operation
     */
    public void queueRemoved(final EntityChunk chunk, final int storageIndex, final boolean destroy) {
        this.removed.add(new Removed(chunk, storageIndex, destroy));
    }

    private static record Removed(EntityChunk chunk, int storageIndex, boolean destroyed) {}
}
