package fi.jakojäännös.roguelite.engine.ecs;

import fi.jakojäännös.roguelite.engine.utilities.IdSupplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;

@Slf4j
public class ComponentStorage<TComponent extends Component> {
    /**
     * <code>ComponentTypeIndex</code> for fast checks if some entity has this type of component.
     *
     * @see Entity#hasComponentBit(int)
     */
    private final int componentTypeIndex;

    private final Queue<StorageTask> taskQueue = new ArrayDeque<>();
    private final ComponentMap<TComponent> componentMap;

    ComponentStorage(int entityCapacity, int componentTypeIndex, @NonNull Function<Integer, TComponent[]> componentArraySupplier) {
        this.componentTypeIndex = componentTypeIndex;
        this.componentMap = new ComponentMap<>(entityCapacity, componentArraySupplier);
    }

    void addComponent(@NonNull Entity entity, @NonNull TComponent component) {
        if (entity.hasComponentBit(this.componentTypeIndex)) {
            LOG.warn("Tried to add component ({}) to an entity, but it already has a component of given type!", component.getClass());
            return;
        }

        this.taskQueue.offer(() -> {
            if (entity.hasComponentBit(this.componentTypeIndex)) {
                LOG.warn("Add task executed when component bit is already set!");
                return;
            }

            this.componentMap.put(entity, component);
            entity.addComponentBit(this.componentTypeIndex);
        });
    }

    void removeComponent(@NonNull Entity entity) {
        if (!entity.hasComponentBit(this.componentTypeIndex)) {
            LOG.warn("Tried to remove component (type: {}) from an entity, but there is none to remove!", this.componentTypeIndex);
            return;
        }

        this.taskQueue.offer(() -> {
            this.componentMap.remove(entity);
            entity.removeComponentBit(this.componentTypeIndex);
        });
    }

    Optional<TComponent> getComponent(@NonNull Entity entity) {
        if (!entity.hasComponentBit(this.componentTypeIndex)) {
            return Optional.empty();
        }

        return this.componentMap.get(entity);
    }

    public void applyModifications() {
        while (!this.taskQueue.isEmpty()) {
            this.taskQueue.remove().execute();
        }
    }

    private interface StorageTask {
        void execute();
    }

    private static class ComponentMap<TComponent> {
        // offset by 1 so that: 0 => null, 1 => 0, 2 => 1, ..., n => n - 1
        private final IdSupplier idSupplier = new IdSupplier();

        private int entityCapacity;
        private int[] entityComponentIndexLookup;

        private final Function<Integer, TComponent[]> componentArraySupplier;
        private final TComponent[] components;

        private ComponentMap(int entityCapacity, Function<Integer, TComponent[]> componentArraySupplier) {
            this.entityCapacity = entityCapacity;
            this.componentArraySupplier = componentArraySupplier;

            this.components = this.componentArraySupplier.apply(entityCapacity);
            this.entityComponentIndexLookup = new int[entityCapacity];
        }

        private Optional<Integer> componentIndexOf(fi.jakojäännös.roguelite.engine.ecs.Entity entity) {
            val index = this.entityComponentIndexLookup[entity.getId()];
            if (index == 0) {
                return Optional.empty();
            }

            return Optional.of(index - 1);
        }

        void put(fi.jakojäännös.roguelite.engine.ecs.Entity entity, TComponent component) {
            val componentIndex = this.idSupplier.get();
            this.entityComponentIndexLookup[entity.getId()] = componentIndex + 1;
            this.components[componentIndex] = component;
        }

        Optional<TComponent> get(fi.jakojäännös.roguelite.engine.ecs.Entity entity) {
            return componentIndexOf(entity)
                    .map(componentIndex -> this.components[componentIndex]);
        }

        void remove(fi.jakojäännös.roguelite.engine.ecs.Entity entity) {
            componentIndexOf(entity)
                    .ifPresent(componentIndex -> {
                        this.components[componentIndex] = null;
                        this.idSupplier.free(componentIndex);
                        this.entityComponentIndexLookup[componentIndex] = 0;
                    });
        }
    }
}
