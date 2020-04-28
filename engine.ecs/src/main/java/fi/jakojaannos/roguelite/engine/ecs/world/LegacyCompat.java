package fi.jakojaannos.roguelite.engine.ecs.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.legacy.Entity;
import fi.jakojaannos.roguelite.engine.ecs.legacy.EntityManager;

public class LegacyCompat implements EntityManager {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyCompat.class);

    private final WorldImpl world;

    @Override
    public Stream<Entity> getAllEntities() {
        return this.world.iterateEntities(new Class[0],
                                          new boolean[0],
                                          new boolean[0],
                                          objects -> new Object(),
                                          false)
                         .map(dataHandle -> dataHandle.getHandle().asLegacyEntity());
    }

    public LegacyCompat(final WorldImpl world) {
        this.world = world;
    }

    @Override
    public Entity createEntity() {
        return (LegacyEntityHandleImpl) this.world.createEntity();
    }

    @Override
    public void destroyEntity(final Entity entity) {
        ((LegacyEntityHandleImpl) entity).destroy();
    }

    @Override
    public void applyModifications() {
        this.world.commitEntityModifications();
    }

    @Override
    public <TComponent> TComponent addComponentTo(
            final Entity entity,
            final TComponent component
    ) {
        ((LegacyEntityHandleImpl) entity).addComponent(component);
        return component;
    }

    @Override
    public void removeComponentFrom(
            final Entity entity,
            final Class<?> componentClass
    ) {
        ((LegacyEntityHandleImpl) entity).removeComponent(componentClass);
    }

    @Override
    public <TComponent> Optional<TComponent> getComponentOf(
            final Entity entity,
            final Class<TComponent> componentClass
    ) {
        return ((LegacyEntityHandleImpl) entity).getComponent(componentClass);
    }

    @Override
    public boolean hasComponent(
            final Entity entity,
            final Class<?> componentClass
    ) {
        return ((LegacyEntityHandleImpl) entity).hasComponent(componentClass);
    }

    @Override
    public <TComponent> Stream<EntityComponentPair<TComponent>> getEntitiesWith(
            final Class<TComponent> componentType
    ) {
        return getEntitiesWith(List.of(componentType), List.of())
                .map(entity -> new EntityComponentPair<>(entity,
                                                         getComponentOf(entity, componentType).orElseThrow()));
    }

    @Override
    public Stream<Entity> getEntitiesWith(
            final Collection<Class<?>> requiredComponents,
            final Collection<Class<?>> excludedComponents
    ) {
        final var components = new Class[requiredComponents.size() + excludedComponents.size()];
        final var excluded = new boolean[components.length];
        var i = 0;
        for (final var component : requiredComponents) {
            components[i] = component;
            ++i;
        }
        for (final var component : excludedComponents) {
            components[i] = component;
            excluded[i] = true;
            ++i;
        }

        return this.world.iterateEntities(components,
                                          excluded,
                                          new boolean[components.length],
                                          objects -> new Object(),
                                          false)
                         .map(dataHandle -> dataHandle.getHandle().asLegacyEntity());
    }
}
