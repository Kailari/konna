package fi.jakojaannos.roguelite.engine.ecs.storage;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import fi.jakojaannos.roguelite.engine.utilities.IdSupplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;

@Slf4j
class ComponentStorage<TComponent extends Component> {
    /**
     * <code>ComponentTypeIndex</code> for fast checks if some entity has this type of component.
     */
    private final int componentTypeIndex;

    private final ComponentMap<TComponent> componentMap;

    ComponentStorage(
            int entityCapacity,
            int componentTypeIndex,
            @NonNull Function<Integer, TComponent[]> componentArraySupplier
    ) {
        this.componentTypeIndex = componentTypeIndex;
        this.componentMap = new ComponentMap<>(entityCapacity, componentArraySupplier);
    }

    void addComponent(@NonNull EntityImpl entity, @NonNull TComponent component) {
        if (BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), this.componentTypeIndex)) {
            LOG.warn("Add task executed when component bit is already set!");
            return;
        }

        this.componentMap.put(entity, component);
        BitMaskUtils.setNthBit(entity.getComponentBitmask(), this.componentTypeIndex);
    }

    void removeComponent(@NonNull EntityImpl entity) {
        this.componentMap.remove(entity);
        BitMaskUtils.unsetNthBit(entity.getComponentBitmask(), this.componentTypeIndex);
    }

    Optional<TComponent> getComponent(@NonNull EntityImpl entity) {
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), this.componentTypeIndex)) {
            return Optional.empty();
        }

        return this.componentMap.get(entity);
    }

    void resize(int entityCapacity) {
        this.componentMap.resize(entityCapacity);
    }

    private static class ComponentMap<TComponent> {
        // offset by 1 so that: 0 => null, 1 => 0, 2 => 1, ..., n => n - 1
        private final IdSupplier idSupplier = new IdSupplier();

        private int entityCapacity;
        private int[] entityComponentIndexLookup;

        private final Function<Integer, TComponent[]> componentArraySupplier;
        private TComponent[] components;

        private ComponentMap(
                int entityCapacity,
                Function<Integer, TComponent[]> componentArraySupplier
        ) {
            this.entityCapacity = entityCapacity;
            this.componentArraySupplier = componentArraySupplier;

            this.components = this.componentArraySupplier.apply(entityCapacity);
            this.entityComponentIndexLookup = new int[entityCapacity];
        }

        private Optional<Integer> componentIndexOf(EntityImpl entity) {
            val index = this.entityComponentIndexLookup[entity.getId()];
            if (index == 0) {
                return Optional.empty();
            }

            return Optional.of(index - 1);
        }

        void put(EntityImpl entity, TComponent component) {
            val componentIndex = this.idSupplier.get();
            this.entityComponentIndexLookup[entity.getId()] = componentIndex + 1;
            this.components[componentIndex] = component;
        }

        Optional<TComponent> get(EntityImpl entity) {
            return componentIndexOf(entity)
                    .map(componentIndex -> this.components[componentIndex]);
        }

        void remove(EntityImpl entity) {
            componentIndexOf(entity)
                    .ifPresent(componentIndex -> {
                        this.components[componentIndex] = null;
                        this.idSupplier.free(componentIndex);
                        this.entityComponentIndexLookup[entity.getId()] = 0;
                    });
        }

        private void resize(int entityCapacity) {
            this.entityCapacity = entityCapacity;
            this.entityComponentIndexLookup = Arrays.copyOf(this.entityComponentIndexLookup, this.entityCapacity);
            this.components = Arrays.copyOf(this.components, this.entityCapacity);
        }
    }
}
