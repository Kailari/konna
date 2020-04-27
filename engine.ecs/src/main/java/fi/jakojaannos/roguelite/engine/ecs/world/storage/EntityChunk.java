package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import javax.annotation.Nullable;

import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

/**
 * Container for fixed number of entity handles and their associated component storage. Chunks form chains in
 * linked-list fashion, allowing them to "grow" on-demand.
 */
public class EntityChunk {
    @Nullable
    private EntityChunk next;

    public int getEntityCount() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Queries whether or not this chunk has a next chunk linked to it. If this returns <code>true</code>, the {@link
     * #next()} getter is guaranteed to return non-null value.
     *
     * @return <code>true</code> if and only if this chunk has another chunk linked to it
     */
    public boolean hasNext() {
        return this.next != null;
    }

    /**
     * Fetches the next chunk in chain. May be <code>null</code> if this is the last chunk in chain.
     *
     * @return the next chained chunk. <code>null</code> if this is the last chunk in this chain
     */
    @Nullable
    public EntityChunk next() {
        return this.next;
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

    private <TComponent> TComponent[] getStorage(final Class<TComponent> componentClass) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public EntityHandle getEntity(final int entityIndex) {
        throw new UnsupportedOperationException("Not implemented");
    }
}
