package fi.jakojaannos.roguelite.engine.ecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Component;
import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;

public class LegacyCompat implements EntityManager {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyCompat.class);

    private final WorldImpl world;

    @Override
    public Stream<Entity> getAllEntities() {
        return IntStream.range(0, LegacyCompat.this.world.getEntityCount())
                        .mapToObj(i -> (LegacyEntityHandleImpl) LegacyCompat.this.world.getEntity(i));
    }

    public LegacyCompat(final WorldImpl world) {
        this.world = world;
    }

    @Override
    public Entity createEntity() {
        return (LegacyEntityHandleImpl) LegacyCompat.this.world.createEntity();
    }

    @Override
    public void destroyEntity(final Entity entity) {
        ((LegacyEntityHandleImpl) entity).destroy();
    }

    @Override
    public void applyModifications() {
        LegacyCompat.this.world.commitEntityModifications();
    }

    @Override
    public <TComponent extends Component> TComponent addComponentTo(
            final Entity entity,
            final TComponent component
    ) {
        ((LegacyEntityHandleImpl) entity).addComponent(component);
        return component;
    }

    @Override
    public void removeComponentFrom(
            final Entity entity,
            final Class<? extends Component> componentClass
    ) {
        ((LegacyEntityHandleImpl) entity).removeComponent(componentClass);
    }

    @Override
    public <TComponent extends Component> Optional<TComponent> getComponentOf(
            final Entity entity,
            final Class<TComponent> componentClass
    ) {
        return ((LegacyEntityHandleImpl) entity).getComponent(componentClass);
    }

    @Override
    public boolean hasComponent(
            final Entity entity,
            final Class<? extends Component> componentClass
    ) {
        return ((LegacyEntityHandleImpl) entity).hasComponent(componentClass);
    }

    @Override
    public <TComponent extends Component> Stream<EntityComponentPair<TComponent>> getEntitiesWith(final Class<? extends TComponent> componentType) {
        return getEntitiesWith(List.of(componentType), List.of())
                .map(entity -> new EntityComponentPair<>(entity,
                                                         getComponentOf(entity, componentType).orElseThrow()));
    }

    @Override
    public Stream<Entity> getEntitiesWith(
            final Collection<Class<? extends Component>> required,
            final Collection<Class<? extends Component>> excluded
    ) {
        final var componentStorage = LegacyCompat.this.world.getComponents();
        return IntStream.range(0, LegacyCompat.this.world.getEntityCount())
                        .filter(id -> required.stream()
                                              .allMatch(c -> componentStorage.has(id, c)))
                        .filter(id -> excluded.stream()
                                              .noneMatch(c -> componentStorage.has(id, c)))
                        .mapToObj(id -> (LegacyEntityHandleImpl) LegacyCompat.this.world.getEntity(id));
    }
}
