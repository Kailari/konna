package fi.jakojaannos.roguelite.engine.ecs.components;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.ComponentGroup;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;

@Slf4j
public class ComponentStorage {
    private final int maxComponentTypes;
    @SuppressWarnings("rawtypes")
    private final Map<Integer, ComponentMap> componentTypes = new HashMap<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();
    private final Map<ComponentGroup, Integer> componentGroupIndices = new HashMap<>();

    private int registeredTypeIndices = 0;
    private int entityCapacity;

    public ComponentStorage(final int entityCapacity, final int maxComponentTypes) {
        this.entityCapacity = entityCapacity;
        this.maxComponentTypes = maxComponentTypes;
    }

    public void clear(final EntityImpl entity) {
        for (final var storage : this.componentTypes.values()) {
            storage.removeComponent(entity);
        }
    }

    public void clear(
            final EntityImpl entity,
            final Class<? extends Component> except
    ) {
        final var componentTypeIndex = getComponentTypeIndexFor(except);
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> i != componentTypeIndex)
                 .forEach(index -> removeComponentByIndex(entity, index));

        checkGroupsAfterRemove(entity, except);
    }

    public void clear(
            final EntityImpl entity,
            final Collection<Class<? extends Component>> except
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
            for (final var storage : this.componentTypes.values()) {
                storage.resize(entityCapacity);
            }
        }
    }

    public void registerGroup(final ComponentGroup group) {
        getComponentTypeIndexFor(group);
    }

    public <TComponent extends Component> void add(
            final EntityImpl entity,
            final TComponent component
    ) {
        final var componentTypeIndex = getComponentTypeIndexFor(component.getClass());
        if (BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            throw new IllegalStateException("Component added while type bit is already set!");
        }
        BitMaskUtils.setNthBit(entity.getComponentBitmask(), componentTypeIndex);

        // noinspection unchecked
        this.componentTypes.get(componentTypeIndex)
                           .addComponent(entity, component);

        checkGroupsAfterAdd(entity, component.getClass());
    }

    public void remove(final EntityImpl entity, final Class<? extends Component> componentClass) {
        final var componentTypeIndex = getComponentTypeIndexFor(componentClass);
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            throw new IllegalStateException("Component removed while type bit is already unset!");
        }
        removeComponentByIndex(entity, componentTypeIndex);

        checkGroupsAfterRemove(entity, componentClass);
    }

    public <TComponent extends Component> Optional<TComponent> get(
            final EntityImpl entity,
            final Class<? extends TComponent> componentClass
    ) {
        final var componentTypeIndex = getComponentTypeIndexFor(componentClass);
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            return Optional.empty();
        }

        final var componentStorage = componentTypes.get(componentTypeIndex);
        // noinspection unchecked
        return (Optional<TComponent>) Optional.ofNullable(componentStorage.getComponent(entity));
    }

    public boolean exists(
            final EntityImpl entity,
            final Class<? extends Component> componentClass
    ) {
        return BitMaskUtils.isNthBitSet(entity.getComponentBitmask(),
                                        getComponentTypeIndexFor(componentClass));
    }

    public boolean anyExists(
            final EntityImpl entity,
            final ComponentGroup group
    ) {
        return BitMaskUtils.isNthBitSet(entity.getComponentBitmask(),
                                        getComponentTypeIndexFor(group));
    }

    public byte[] createComponentBitmask(
            final Collection<Class<? extends Component>> componentTypes
    ) {
        return componentTypes.stream()
                             .map(this::getComponentTypeIndexFor)
                             .reduce(new byte[BitMaskUtils.calculateMaskSize(this.maxComponentTypes)],
                                     BitMaskUtils::setNthBit,
                                     BitMaskUtils::combineMasks);
    }

    public byte[] createComponentBitmask(
            final Collection<Class<? extends Component>> componentTypes,
            final Collection<ComponentGroup> componentGroups
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
            final Class<? extends Component> componentClass
    ) {
        return this.componentTypeIndices.computeIfAbsent(componentClass,
                                                         this::createNewComponentStorage);
    }

    private int getComponentTypeIndexFor(
            final ComponentGroup group
    ) {
        return this.componentGroupIndices.computeIfAbsent(group,
                                                          this::createNewComponentGroup);
    }

    private int createNewComponentGroup(final ComponentGroup group) {
        LOG.trace("Created new component group {}", group.getName());
        return getNextComponentTypeIndex();
    }

    private int getNextComponentTypeIndex() {
        final var index = this.registeredTypeIndices;
        ++this.registeredTypeIndices;

        if (index >= this.maxComponentTypes) {
            throw new IllegalStateException("Too many component types registered!");
        }
        return index;
    }

    private int createNewComponentStorage(final Class<? extends Component> componentClass) {
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
            final EntityImpl entity,
            final int componentTypeIndex
    ) {
        this.componentTypes.get(componentTypeIndex)
                           .removeComponent(entity);

        BitMaskUtils.unsetNthBit(entity.getComponentBitmask(), componentTypeIndex);
    }

    private void checkGroupsAfterAdd(
            final EntityImpl entity,
            final Class<? extends Component> added
    ) {
        this.componentGroupIndices.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getKey().getComponentTypes().contains(added))
                                  .map(Map.Entry::getValue)
                                  .forEach(groupComponentTypeIndex ->
                                                   BitMaskUtils.setNthBit(entity.getComponentBitmask(),
                                                                          groupComponentTypeIndex));
    }

    private void checkGroupsAfterRemove(
            final EntityImpl entity,
            final Class<? extends Component> removed
    ) {
        this.componentGroupIndices.entrySet()
                                  .stream()
                                  .filter(entry -> entry.getKey().getComponentTypes().contains(removed))
                                  .filter(entry -> !anyExists(entity, entry.getKey()))
                                  .map(Map.Entry::getValue)
                                  .forEach(groupComponentTypeIndex ->
                                                   BitMaskUtils.unsetNthBit(entity.getComponentBitmask(),
                                                                            groupComponentTypeIndex));
    }
}
