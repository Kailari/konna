package fi.jakojaannos.roguelite.engine.ecs.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;
import fi.jakojaannos.roguelite.engine.utilities.BitMaskUtils;

public class ComponentStorage {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentStorage.class);

    private final int maxComponentTypes;
    @SuppressWarnings("rawtypes")
    private final Map<Integer, ComponentMap> componentTypes = new HashMap<>();
    private final Map<Class<? extends Component>, Integer> componentTypeIndices = new HashMap<>();

    private int registeredTypeIndices;
    private int entityCapacity;

    private int getNextComponentTypeIndex() {
        final var index = this.registeredTypeIndices;
        ++this.registeredTypeIndices;

        if (index >= this.maxComponentTypes) {
            throw new IllegalStateException("Too many component types registered!");
        }
        return index;
    }

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
    }

    public void clear(
            final EntityImpl entity,
            final Collection<Class<? extends Component>> except
    ) {
        final var allowedIndices = except.stream()
                                         .map(this::getComponentTypeIndexFor)
                                         .collect(Collectors.toList());
        IntStream.range(0, this.componentTypes.size())
                 .filter(i -> !allowedIndices.contains(i))
                 .forEach(index -> removeComponentByIndex(entity, index));
    }

    public void resize(final int entityCapacity) {
        if (entityCapacity > this.entityCapacity) {
            this.entityCapacity = entityCapacity;
            for (final var storage : this.componentTypes.values()) {
                storage.resize(entityCapacity);
            }
        }
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
    }

    public void remove(final EntityImpl entity, final Class<? extends Component> componentClass) {
        final var componentTypeIndex = getComponentTypeIndexFor(componentClass);
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            throw new IllegalStateException("Component removed while type bit is already unset!");
        }
        removeComponentByIndex(entity, componentTypeIndex);
    }

    public <TComponent extends Component> Optional<TComponent> get(
            final EntityImpl entity,
            final Class<? extends TComponent> componentClass
    ) {
        final var componentTypeIndex = getComponentTypeIndexFor(componentClass);
        if (!BitMaskUtils.isNthBitSet(entity.getComponentBitmask(), componentTypeIndex)) {
            return Optional.empty();
        }

        final var componentStorage = this.componentTypes.get(componentTypeIndex);
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

    public byte[] createComponentBitmask(
            final Collection<Class<? extends Component>> componentTypes
    ) {
        return componentTypes.stream()
                             .map(this::getComponentTypeIndexFor)
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

    private int createNewComponentStorage(final Class<? extends Component> componentClass) {
        final int index = getNextComponentTypeIndex();
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
}
