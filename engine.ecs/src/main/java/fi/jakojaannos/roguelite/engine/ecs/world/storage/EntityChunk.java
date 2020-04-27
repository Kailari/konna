package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.world.EntityHandleImpl;

/**
 * Container for fixed number of entity handles and their associated component storage. Chunks form chains in
 * linked-list fashion, allowing them to "grow" on-demand.
 */
public class EntityChunk {
    private static final int ENTITY_COUNT_PER_CHUNK = 256;

    @SuppressWarnings("rawtypes")
    private final Map<Class, Object[]> components;
    private final EntityHandleImpl[] entities;
    private final Archetype archetype;

    private final AtomicInteger indexCounter = new AtomicInteger(0);
    private int nEntities;

    @Nullable
    private EntityChunk next;

    public int getEntityCount() {
        return this.nEntities;
    }

    public int getLastEntityIndex() {
        return this.indexCounter.get();
    }

    /**
     * Fetches the next chunk in chain. May be <code>null</code> if this is the last chunk in chain.
     *
     * @return the next chained chunk. <code>null</code> if this is the last chunk in this chain
     */
    @Nullable
    public EntityChunk getNext() {
        return this.next;
    }

    public Archetype getArchetype() {
        return this.archetype;
    }

    public EntityChunk(final Archetype archetype) {
        this.archetype = archetype;

        this.entities = new EntityHandleImpl[ENTITY_COUNT_PER_CHUNK];
        this.components = Arrays.stream(archetype.getComponentClasses())
                                .collect(Collectors.toUnmodifiableMap(clazz -> clazz,
                                                                      EntityChunk::constructComponentArray));
    }

    /**
     * Gets handle for the entity with the given storage index.
     *
     * @param storageIndex the storage index for the entity
     *
     * @return the entity
     */
    public EntityHandleImpl getEntity(final int storageIndex) {
        return this.entities[storageIndex];
    }

    /**
     * Queries whether or not this chunk has a next chunk linked to it. If this returns <code>true</code>, the {@link
     * #getNext()} getter is guaranteed to return non-null value.
     *
     * @return <code>true</code> if and only if this chunk has another chunk linked to it
     */
    public boolean hasNext() {
        return this.next != null;
    }

    /**
     * Stores the given entity to this chunk. If this chunk is full, the entity is stored to the next chunk. If there is
     * no next chunk, one is created and linked as the {@link #getNext() next} for this chunk.
     *
     * @param entityHandle     handle of the entity to add
     * @param componentClasses component classes, these must exactly match the ones stored in this chunk, but the order
     *                         does not matter
     * @param components       components, in the order specified by the <code>componentClasses</code> parameter
     */
    public void addEntity(
            final EntityHandleImpl entityHandle,
            final Class<?>[] componentClasses,
            final Object[] components
    ) {
        final var isFull = this.indexCounter.get() == ENTITY_COUNT_PER_CHUNK - 1;
        if (isFull) {
            if (this.next == null) {
                this.next = new EntityChunk(this.archetype);
            }
            this.next.addEntity(entityHandle, componentClasses, components);
            return;
        }

        final var index = this.indexCounter.getAndIncrement();
        this.entities[index] = entityHandle;
        for (int i = 0; i < componentClasses.length; i++) {
            final var storage = getStorage(componentClasses[i]);
            if (storage == null) {
                throw new IllegalArgumentException("Tried adding entity with component of type \""
                                                   + componentClasses[i].getSimpleName()
                                                   + "\" not stored in this chunk!");
            }

            storage[index] = components[i];
        }
        entityHandle.moveToChunk(this, index);
    }

    /**
     * Fetches the storage arrays for given component types. The storages are placed in the exact order to match the
     * given component classes.
     * <p>
     * Used for pre-fetching relevant storages for {@link EntityChunkSpliterator entity spliterators}.
     *
     * @param componentClasses component classes to fetch the storages for
     *
     * @return array of component storages
     *
     * @throws IllegalArgumentException if any of the given component types <i>(not marked as optional)</i> does not
     *                                  match the component types on entities stored in this chunk
     */
    public Object[][] fetchStorages(
            final Class<?>[] componentClasses,
            final boolean[] optional
    ) {
        final var paramStorages = new Object[componentClasses.length][];

        for (int index = 0; index < paramStorages.length; ++index) {
            final Class<?> componentClass = componentClasses[index];
            final Object[] storage = getStorage(componentClass);

            if (storage == null && !optional[index]) {
                throw new IllegalArgumentException("Tried fetching component type \""
                                                   + componentClass.getSimpleName()
                                                   + "\" not stored in this chunk!");
            }
            paramStorages[index] = storage;
        }
        return paramStorages;
    }

    /**
     * Commits addition operations to this chunk. Makes added entities visible. Should always be called before any
     * entity swapping is performed.
     */
    public void commitAdditions() {
        this.nEntities = this.indexCounter.get();
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <TComponent> TComponent[] getStorage(final Class<TComponent> componentClass) {
        return (TComponent[]) this.components.get(componentClass);
    }

    /**
     * Moves the last entity in this chunk into the given other chunk.
     *
     * @param chunk        the target chunk
     * @param storageIndex the storage index in the target chunk
     */
    synchronized void moveLastInto(final EntityChunk chunk, final int storageIndex) {
        // SAFETY: It is safe to get first and then decrement later as this method is always run in
        //         the main thread. Worker threads should never call this, or it must be ensured
        //         that a single call at a time is in progress (method is `synchronized`)
        if (this.indexCounter.get() == 0) {
            return;
        }

        final var lastIndex = this.indexCounter.decrementAndGet();
        for (final Class<?> componentClass : this.getArchetype().getComponentClasses()) {
            moveComponent(this, chunk, lastIndex, storageIndex, componentClass);
        }
        final var handle = this.entities[lastIndex];
        chunk.entities[storageIndex] = handle;
        this.entities[lastIndex] = null;

        handle.moveToChunk(chunk, storageIndex);
    }

    public <TComponent> TComponent getComponent(
            final int storageIndex,
            final Class<TComponent> componentClass
    ) {
        final var storage = getStorage(componentClass);
        if (storage == null) {
            throw new IllegalArgumentException("Tried getting component of type \""
                                               + componentClass.getSimpleName()
                                               + "\" not stored in this chunk!");
        }

        return storage[storageIndex];
    }

    private static <TComponent> void moveComponent(
            final EntityChunk from,
            final EntityChunk to,
            final int fromIndex,
            final int toIndex,
            final Class<TComponent> componentClass
    ) {
        final var fromStorage = from.getStorage(componentClass);
        final var toStorage = to.getStorage(componentClass);
        if (fromStorage == null || toStorage == null) {
            throw new IllegalArgumentException("Tried swapping component of type \""
                                               + componentClass.getSimpleName()
                                               + "\" not stored in this chunk!");
        }

        toStorage[toIndex] = fromStorage[fromIndex];
    }

    @SuppressWarnings("unchecked")
    private static <TComponent> TComponent[] constructComponentArray(
            final Class<TComponent> componentClass
    ) {
        return (TComponent[]) Array.newInstance(componentClass, ENTITY_COUNT_PER_CHUNK);
    }
}
