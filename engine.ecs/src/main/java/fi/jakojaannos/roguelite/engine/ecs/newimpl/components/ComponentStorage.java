package fi.jakojaannos.roguelite.engine.ecs.newimpl.components;

import java.util.Spliterator;
import java.util.function.Consumer;

// TODO: add/remove must happen *after* iteration (of a single system) has finished. Otherwise
//       execution order might affect which entities are iterated

public final class ComponentStorage {
    /*private final byte[][] entityBitMasks;
    private final ComponentStorageMap components = new ComponentStorageMap();

    public <TComponent> Stream<TComponent> getAll(final Class<TComponent> componentClass) {
        final int componentTypeId = getTypeId(componentClass);
        return StreamSupport.stream(new ComponentSpliterator<TComponent>(0,
                                                                         nComponents,
                                                                         componentTypeId),
                                    false);
    }

    private <TComponent> int getTypeId(final Class<TComponent> componentClass) {
        // TODO
        return 0;
    }

    public <TComponent> boolean addComponent(final int entityId, final TComponent component) {

    }

    public <TComponent> boolean hasComponent(final int entityId, final Class<TComponent> component) {

    }

    public <TComponent> boolean removeComponent(final int entityId, final Class<TComponent> componentClass) {

    }

    public <TTag> boolean tag(final int entityId, final Class<TTag> tagClass) {

    }

    public <TTag> boolean hasTag(final int entityId, final Class<TTag> tagClass) {

    }

    public <TTag> boolean removeTag(final int entityId, final Class<TTag> tagClass) {

    }

    private static class EntityDataSpliterator<TEntityData> implements Spliterator<TEntityData> {
        private final Map<Class, ComponentSpliterator> componentSpliterators;

        private int startId;
        private int endId;

        ComponentSpliterator(
                final int startId,
                final int endId,
                final Map<Class, ComponentSpliterator> componentSpliterators
        ) {
            this.startId = startId;
            this.endId = endId;
            this.componentSpliterators = componentSpliterators;
        }

        @Override
        public boolean tryAdvance(final Consumer<? super TEntityData> action) {
            // Skip elements until an entity has the specified component
            while (this.components[this.startId] == null) {
                this.startId++;
                if (this.startId >= this.endId) {
                    return false;
                }
            }

            action.accept(this.components[this.startId]);
            this.startId++;
            return true;
        }

        @Override
        public Spliterator<TEntityData> trySplit() {
            final var remaining = this.endId - this.startId;
            if (remaining >= 2) {
                final var oldEndId = this.endId;
                this.endId = remaining / 2;

                return new ComponentSpliterator<>(this.endId + 1, oldEndId, this.components);
            }

            return null;
        }

        @Override
        public long estimateSize() {
            return this.endId - this.startId;
        }

        @Override
        public int characteristics() {
            return DISTINCT | SIZED | IMMUTABLE;
        }
    }
    */
}
