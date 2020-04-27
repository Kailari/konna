package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

// TODO: validate component storage nullness checks etc.
//      - storage should now never contain null components on entities

/**
 * Spliterator over a chunk chain. Produces entity data handles, containing pre-determined components pre-fetched.
 * <p>
 * Split policy is to split whenever the current chunk has next chunk attached to it <i>(chunk chain head moves to next
 * chunk, current becomes "leaf")</i>, and split leaves to regions, halving region length on each split.
 *
 * @param <TEntityData>
 */
public class EntityChunkSpliterator<TEntityData> implements Spliterator<EntityDataHandle<TEntityData>> {
    private final Class<?>[] componentClasses;
    private final boolean[] optional;
    private final Function<Object[], TEntityData> dataFactory;
    private final Object[][] relevantStorages;
    private EntityChunk chunk;
    private boolean chained;

    private int startPointer;
    private int endPointer;

    public EntityChunkSpliterator(
            final EntityChunk chunk,
            final Class<?>[] componentClasses,
            final boolean[] optional,
            final Function<Object[], TEntityData> dataFactory
    ) {
        this.chunk = chunk;
        this.componentClasses = componentClasses;
        this.optional = optional;
        this.dataFactory = dataFactory;

        this.relevantStorages = chunk.fetchStorages(componentClasses, optional);

        this.chained = this.chunk.hasNext();
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
            consumer.accept(new EntityDataHandleImpl<>(this.dataFactory.apply(fetchParameters(this.startPointer)),
                                                       fetchHandle(this.startPointer)));
            ++this.startPointer;
            return true;
        }

        // 2. exhausted chained chunk, swap chunk to next and try advance there
        if (this.chained) {
            this.chunk = this.chunk.next();
            assert this.chunk != null;

            this.chained = this.chunk.hasNext();
            this.startPointer = 0;
            this.endPointer = this.chunk.getEntityCount();

            // FIXME: Replace recursion with wrapping while-loop
            return tryAdvance(consumer);
        }

        // 3. exhausted non-chained, return false
        return false;
    }

    private EntityHandle fetchHandle(final int index) {
        return this.chunk.getEntity(index);
    }

    private Object[] fetchParameters(final int entityIndex) {
        final var parameters = new Object[this.relevantStorages.length];
        for (int paramIndex = 0; paramIndex < parameters.length; ++paramIndex) {
            parameters[paramIndex] = this.relevantStorages[paramIndex][entityIndex];

            final var isNull = parameters[paramIndex] == null;

            final var isOptional = this.optional[paramIndex];
            // Wrap if optional
            if (isOptional) {
                parameters[paramIndex] = Optional.ofNullable(parameters[paramIndex]);
            }
            // Required component not present; request is invalid!
            else if (isNull) {
                throw new IllegalStateException("Required component not present!");
            }
        }

        return parameters;
    }

    @Override
    public Spliterator<EntityDataHandle<TEntityData>> trySplit() {
        // Un-chain this chunk if we are still part of a chain
        if (this.chunk.hasNext()) {
            final var next = this.chunk.next();
            assert next != null;

            this.chained = false;
            return new EntityChunkSpliterator<>(next,
                                                this.componentClasses,
                                                this.optional,
                                                this.dataFactory);
        } else {
            final var remaining = this.endPointer - this.startPointer;
            // Halve the region if possible
            if (remaining >= 2) {
                final var oldEndPointer = this.endPointer;
                this.endPointer = this.startPointer + remaining / 2;

                return new EntityChunkSpliterator<>(this.chunk,
                                                    this.componentClasses,
                                                    this.optional,
                                                    this.dataFactory);
            }
        }

        // Could not split, return null
        return null;
    }

    @Override
    public long estimateSize() {
        return 0;
    }

    @Override
    public int characteristics() {
        return 0;
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
