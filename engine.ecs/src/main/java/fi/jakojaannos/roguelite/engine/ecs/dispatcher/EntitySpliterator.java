package fi.jakojaannos.roguelite.engine.ecs.dispatcher;

import java.util.Optional;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import fi.jakojaannos.roguelite.engine.ecs.EcsSystem;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.World;

class EntitySpliterator<TEntityData> implements Spliterator<EcsSystem.EntityDataHandle<TEntityData>> {
    private final Object[][] paramStorages;
    private final Object[] parameters;
    private final World world;

    private final Function<Object[], TEntityData> factory;

    private int startIndex;
    private int endIndex;

    EntitySpliterator(
            final Class<?>[] componentClasses,
            final World world,
            final Function<Object[], TEntityData> factory
    ) {
        this(world.getComponents().fetchStorages(componentClasses),
             world,
             factory,
             0,
             world.getEntityCount());
    }

    private EntitySpliterator(
            final Object[][] paramStorages,
            final World world,
            final Function<Object[], TEntityData> factory,
            final int startIndex,
            final int endIndex
    ) {
        this.parameters = new Object[paramStorages.length];
        this.paramStorages = paramStorages;
        this.world = world;
        this.factory = factory;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    @Override
    public boolean tryAdvance(final Consumer<? super EcsSystem.EntityDataHandle<TEntityData>> action) {
        // Do nothing if there is nothing more to iterate
        if (this.startIndex >= this.endIndex) {
            return false;
        }

        // Skip elements until an entity has all of the specified components. This also updates
        // the `parameters` array.
        while (this.tryFetchParameters(this.startIndex)) {
            this.startIndex++;
            if (this.startIndex >= this.endIndex) {
                return false;
            }
        }

        action.accept(new EntityDataHandleImpl<>(this.factory.apply(this.parameters),
                                                 this.world.getEntity(this.startIndex)));
        this.startIndex++;
        return true;
    }

    @Override
    public Spliterator<EcsSystem.EntityDataHandle<TEntityData>> trySplit() {
        final var remaining = this.endIndex - this.startIndex;
        if (remaining >= 2) {
            final var oldEndIndex = this.endIndex;
            this.endIndex = this.startIndex + remaining / 2;

            return new EntitySpliterator<>(this.paramStorages,
                                           this.world,
                                           this.factory,
                                           this.endIndex,
                                           oldEndIndex);
        }

        return null;
    }

    @Override
    public long estimateSize() {
        return this.endIndex - this.startIndex;
    }

    @Override
    public int characteristics() {
        return DISTINCT | SIZED | IMMUTABLE;
    }

    /**
     * Try to fetch parameters for given entity ID into the parameters array.
     *
     * @param entityId the entity ID
     *
     * @return <code>true</code> if parameters are incomplete
     */
    private boolean tryFetchParameters(final int entityId) {
        for (int paramIndex = 0; paramIndex < this.parameters.length; ++paramIndex) {
            this.parameters[paramIndex] = this.paramStorages[paramIndex][entityId];

            if (this.parameters[paramIndex] == null) {
                return true;
            }
        }

        return false;
    }

    private static class EntityDataHandleImpl<TEntityData> implements EcsSystem.EntityDataHandle<TEntityData> {
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
