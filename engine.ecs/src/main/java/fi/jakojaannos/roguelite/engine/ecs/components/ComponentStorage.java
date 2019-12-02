package fi.jakojaannos.roguelite.engine.ecs.components;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ComponentGroup;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Slf4j
public class ComponentStorage {
    private final int maxComponentTypes;
    private final Map<Integer, ComponentMap> componentTypes = new HashMap<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();
    private final Map<ComponentGroup, Integer> componentGroupIndices = new HashMap<>();

    private int registeredTypeIndices = 0;
    private int entityCapacity;

    public ComponentStorage(int entityCapacity, int maxComponentTypes) {
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
    }

    public void clear(@NonNull final EntityImpl entity) {
        for (val storage : this.componentTypes.values()) {
            storage.removeComponent(entity);
        }
    }

    public void clear(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends Component> except
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(except);
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> i != componentTypeIndex)
                 .forEach(index -> removeComponentByIndex(entity, index));

        checkGroupsAfterRemove(entity, except);
    }

    public void clear(
            @NonNull final EntityImpl entity,
            @NonNull final Collection<Class<? extends Component>> except
    ) {
        List<Integer> allowedIndices = except.stream()
                                             .map(this::getComponentTypeIndexFor)
                                             .collect(Collectors.toList());
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> !allowedIndices.contains(i))
                 .forEach(index -> removeComponentByIndex(entity, index));

        except.forEach(removed -> checkGroupsAfterRemove(entity, removed));
    }

    public void resize(final int entityCapacity) {
        if (entityCapacity > this.entityCapacity) {
            this.entityCapacity = entityCapacity;
            for (val storage : this.componentTypes.values()) {
                storage.resize(entityCapacity);
            }
        }
    }

    public void registerGroup(@NonNull final ComponentGroup group) {
        getComponentTypeIndexFor(group);
    }

    public <TComponent extends Component> void add(
            @NonNull final EntityImpl entity,
            @NonNull final TComponent component
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(component.getClass());
        if (BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            throw new IllegalStateException("Component added while type bit is already set!");
        }
        BitMaskUtils.setNthBit(entity.getComponentBitmask(), componentTypeIndex);

        // noinspection unchecked
        this.componentTypes.get(componentTypeIndex)
                           .addComponent(entity, component);

        checkGroupsAfterAdd(entity, component.getClass());
    }

    public void remove(EntityImpl entity, Class<? extends Component> componentClass) {
        val componentTypeIndex = getComponentTypeIndexFor(componentClass);
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            throw new IllegalStateException("Component removed while type bit is already unset!");
        }
        removeComponentByIndex(entity, componentTypeIndex);

        checkGroupsAfterRemove(entity, componentClass);
    }

    public <TComponent extends Component> Optional<TComponent> get(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends TComponent> componentClass
    ) {
        val componentTypeIndex = getComponentTypeIndexFor(componentClass);
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            return Optional.empty();
        }

        val componentStorage = componentTypes.get(componentTypeIndex);
        // noinspection unchecked
        return (Optional<TComponent>) Optional.of(componentStorage.getComponent(entity));
    }

    public boolean exists(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends Component> componentClass
    ) {
        return BitMaskUtils.isNthBitSet(entity.getComponentBitmask(),
                                        getComponentTypeIndexFor(componentClass));
    }

    public boolean anyExists(
            @NonNull final EntityImpl entity,
            @NonNull final ComponentGroup group
    ) {
        return BitMaskUtils.isNthBitSet(entity.getComponentBitmask(),
                                        getComponentTypeIndexFor(group));
    }

    public byte[] createComponentBitmask(
            @NonNull final Collection<Class<? extends Component>> componentTypes
    ) {
        return componentTypes.stream()
                             .map(this::getComponentTypeIndexFor)
                             .reduce(new byte[BitMaskUtils.calculateMaskSize(this.maxComponentTypes)],
                                     BitMaskUtils::setNthBit,
                                     BitMaskUtils::combineMasks);
    }

    public byte[] createComponentBitmask(
            @NonNull final Collection<Class<? extends Component>> componentTypes,
            @NonNull final Collection<ComponentGroup> componentGroups
    ) {
        return Stream.concat(componentTypes.stream()
                                           .map(this::getComponentTypeIndexFor),
                             componentGroups.stream()
                                            .map(this::getComponentTypeIndexFor))
                     .reduce(new byte[BitMaskUtils.calculateMaskSize(this.maxComponentTypes)],
                             BitMaskUtils::setNthBit,
                             BitMaskUtils::combineMasks);
    }

    private int getComponentTypeIndexFor(
            @NonNull final Class<? extends Component> componentClass
    ) {
        return this.componentTypeIndices.computeIfAbsent(componentClass,
                                                         this::createNewComponentStorage);
    }

    private int getComponentTypeIndexFor(
            @NonNull final ComponentGroup group
    ) {
        return this.componentGroupIndices.computeIfAbsent(group,
                                                          this::createNewComponentGroup);
    }

    private int createNewComponentGroup(@NonNull final ComponentGroup group) {
        LOG.trace("Created new component group {}", group.getName());
        return getNextComponentTypeIndex();
    }

    private int getNextComponentTypeIndex() {
        val index = this.registeredTypeIndices;
        ++this.registeredTypeIndices;

        if (index >= this.maxComponentTypes) {
            throw new IllegalStateException("Too many component types registered!");
        }
        return index;
    }

    private int createNewComponentStorage(@NonNull Class<? extends Component> componentClass) {
        int index = getNextComponentTypeIndex();
        this.componentTypes.put(index, new ComponentMap<>(
                this.entityCapacity,
                componentClass
        ));

        LOG.trace("Created new component storage {} with capacity {}",
                  componentClass.getSimpleName(),
                  this.entityCapacity);
        return index;
    }

    private void removeComponentByIndex(
            @NonNull final EntityImpl entity,
            final int componentTypeIndex
    ) {
        this.componentTypes.get(componentTypeIndex)
                           .removeComponent(entity);

        BitMaskUtils.unsetNthBit(entity.getComponentBitmask(), componentTypeIndex);
    }

    private void checkGroupsAfterAdd(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends Component> added
    ) {
        this.componentGroupIndices.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getKey().getComponentTypes().contains(added))
                                  .map(Map.Entry::getValue)
                                  .forEach(groupComponentTypeIndex -> BitMaskUtils.setNthBit(entity.getComponentBitmask(), groupComponentTypeIndex));
    }

    private void checkGroupsAfterRemove(
            @NonNull final EntityImpl entity,
            @NonNull final Class<? extends Component> removed
    ) {
        this.componentGroupIndices.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getKey().getComponentTypes().contains(removed))
                                  .filter(entry -> !anyExists(entity, entry.getKey()))
                                  .map(Map.Entry::getValue)
                                  .forEach(groupComponentTypeIndex -> BitMaskUtils.unsetNthBit(entity.getComponentBitmask(), groupComponentTypeIndex));
    }
}
