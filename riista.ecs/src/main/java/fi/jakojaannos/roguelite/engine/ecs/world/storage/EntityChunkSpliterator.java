package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

/**
 * Spliterator over a chunk chain. Produces entity data handles, containing pre-determined components pre-fetched.
 * <p>
 * Split policy is to split whenever the current chunk has next chunk attached to it <i>(chunk chain head moves to next
 * chunk, current becomes "leaf")</i>, and split leaves to regions, halving region length on each split.
 *
 * @param <TEntityData>
 */
public class EntityChunkSpliterator<TEntityData> implements Spliterator<EntityDataHandle<TEntityData>> {
    private final Function<Object[], TEntityData> dataFactory;
    private final Object[][] storages;
    private final Class<?>[] componentClasses;
    private final boolean[] excluded;
    private final boolean[] optional;

    private EntityChunk chunk;
    private int startPointer;
    private int endPointer;

    /**
     * Marks the "head" spliterator. The first spliterator in chain has this set to <code>true</code>, indicating that
     * it should always check the next chunk once it has exhausted the previous one.
     */
    private boolean chained;

    public EntityChunkSpliterator(
            final EntityChunk chunk,
            final Class<?>[] componentClasses,
            final boolean[] optional,
            final boolean[] excluded,
            final Function<Object[], TEntityData> dataFactory
    ) {
        this(chunk,
             componentClasses,
             chunk.fetchStorages(componentClasses, optional, excluded),
             optional,
             excluded,
             dataFactory,
             0,
             chunk.getEntityCount(),
             true);
    }

    private EntityChunkSpliterator(
            final EntityChunk chunk,
            final Class<?>[] componentClasses,
            final Object[][] storages,
            final boolean[] optional,
            final boolean[] excluded,
            final Function<Object[], TEntityData> dataFactory,
            final int startPointer,
            final int endPointer,
            final boolean chained
    ) {
        this.chunk = chunk;
        this.componentClasses = componentClasses;
        this.storages = storages;
        this.optional = optional;
        this.excluded = excluded;
        this.dataFactory = dataFactory;

        this.startPointer = startPointer;
        this.endPointer = endPointer;

        this.chained = chained;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super EntityDataHandle<TEntityData>> consumer) {
        // Options what we may need to do here:
        //  1. this is chunk with entities remaining, fetch components and return `true`
        //  2. this is chained chunk with no more entities, swap the chunk to the next chunk and
        //     take the first entity available there
        //  3. this is non-chained chunk (no next chunk or next is handled by another instance) with
        //     no more entities; do nothing and return `false`

        // 1. Entities remaining
        if (this.startPointer < this.endPointer) {
            final var handle = fetchHandle(this.startPointer);

            consumer.accept(new EntityDataHandleImpl<>(this.dataFactory.apply(fetchParameters(this.startPointer)),
                                                       handle));

            ++this.startPointer;
            return true;
        }

        // 2. exhausted chained chunk, swap chunk to next and try advance there
        if (this.chained && this.chunk.hasNext()) {
            this.chunk = this.chunk.getNext();
            assert this.chunk != null;

            this.startPointer = 0;
            this.chained = true;
            this.endPointer = this.chunk.getEntityCount();

            // FIXME: Replace recursion with wrapping while-loop
            return tryAdvance(consumer);
        }

        // 3. exhausted non-chained; nothing to do, return false
        return false;
    }

    private EntityHandle fetchHandle(final int index) {
        return this.chunk.getEntity(index);
    }

    private Object[] fetchParameters(final int entityIndex) {
        final var parameters = new Object[this.storages.length];
        for (int paramIndex = 0; paramIndex < parameters.length; ++paramIndex) {
            final var storage = this.storages[paramIndex];
            parameters[paramIndex] = storage != null ? storage[entityIndex] : null;

            final var isNull = parameters[paramIndex] == null;

            final var isOptional = this.optional[paramIndex];
            final var isExcluded = this.excluded[paramIndex];

            // Wrap if optional
            if (isOptional) {
                parameters[paramIndex] = Optional.ofNullable(parameters[paramIndex]);
            }
            // Required component not present? request is invalid!
            else if (isNull && !isExcluded) {
                throw new IllegalStateException("Required component not present!");
            }
        }

        return parameters;
    }

    @Override
    public Spliterator<EntityDataHandle<TEntityData>> trySplit() {
        // Un-chain this chunk if we are still part of a chain
        if (this.chunk.hasNext()) {
            assert this.chunk.getNext() != null;

            // No longer head, un-chain this spliterator (the new spliterator is the new head)
            this.chained = false;
            return new EntityChunkSpliterator<>(this.chunk.getNext(),
                                                this.componentClasses,
                                                this.optional,
                                                this.excluded,
                                                this.dataFactory);
        }
        // Halve the region if possible
        else {
            final var remaining = this.endPointer - this.startPointer;
            if (remaining >= 2) {
                // Move self to handle only the second half (preserves the head status)
                final var oldStartPointer = this.startPointer;
                this.startPointer = this.startPointer + remaining / 2;

                // Create new tail (non-head) spliterator for the first half
                return new EntityChunkSpliterator<>(this.chunk,
                                                    this.componentClasses,
                                                    this.storages,
                                                    this.optional,
                                                    this.excluded,
                                                    this.dataFactory,
                                                    oldStartPointer,
                                                    this.startPointer,
                                                    false);
            }
        }

        // Could not split, return null
        return null;
    }

    @Override
    public long estimateSize() {
        var size = this.endPointer - this.startPointer;
        var current = this.chunk.getNext();
        while (current != null) {
            size += current.getEntityCount();
            current = current.getNext();
        }

        return size;
    }

    @Override
    public int characteristics() {
        return DISTINCT | SIZED | IMMUTABLE;
    }

    private static class EntityDataHandleImpl<TEntityData> implements EntityDataHandle<TEntityData> {
        private final TEntityData data;
        private final EntityHandle handle;

        @Override
        public boolean isPendingRemoval() {
            return this.handle.isPendingRemoval();
        }

        @Override
        public int getId() {
            return this.handle.getId();
        }

        @Override
        public EntityHandle getHandle() {
            return this.handle;
        }

        @Override
        public TEntityData getData() {
            return this.data;
        }

        private EntityDataHandleImpl(final TEntityData data, final EntityHandle handle) {
            this.data = data;
            this.handle = handle;
        }

        @Override
        public <TComponent> boolean addComponent(final TComponent component) {
            return this.handle.addComponent(component);
        }

        @Override
        public <TComponent> TComponent addOrGet(
                final Class<TComponent> componentClass,
                final Supplier<TComponent> supplier
        ) {
            return this.handle.addOrGet(componentClass, supplier);
        }

        @Override
        public <TComponent> boolean removeComponent(final Class<TComponent> componentClass) {
            return this.handle.removeComponent(componentClass);
        }

        @Override
        public <TComponent> boolean hasComponent(final Class<TComponent> componentClass) {
            return this.handle.hasComponent(componentClass);
        }

        @Override
        public <TComponent> Optional<TComponent> getComponent(final Class<TComponent> componentClass) {
            return this.handle.getComponent(componentClass);
        }

        @Override
        public void destroy() {
            this.handle.destroy();
        }
    }
}
