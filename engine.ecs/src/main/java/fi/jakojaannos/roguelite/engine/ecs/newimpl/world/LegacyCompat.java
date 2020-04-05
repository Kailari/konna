package fi.jakojaannos.roguelite.engine.ecs.newimpl.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.*;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.World;
import fi.jakojaannos.roguelite.engine.ecs.world.WorldImpl;

public class LegacyCompat implements fi.jakojaannos.roguelite.engine.ecs.World {
    private static final Logger LOG = LoggerFactory.getLogger(LegacyCompat.class);

    private static final boolean LOG_GET_OR_CREATE = false;
    private static final boolean LOG_PROVIDE = false;
    private static final boolean LOG_GET = false;

    private final World world;

    private final EntityManagerImpl em;

    @Override
    public EntityManager getEntityManager() {
        return this.em;
    }

    public LegacyCompat(final World world) {
        this.world = world;
        this.em = new EntityManagerImpl();
    }

    @Override
    public <TResource extends Resource> TResource getOrCreateResource(
            final Class<TResource> resourceClass
    ) {
        if (LOG_GET_OR_CREATE) {
            LOG.warn("getOrCreateResource called!");
        }

        try {
            return this.world.fetchResource(resourceClass);
        } catch (final Throwable e) {
            //LOG.warn("getOrCreateResource with fetchResource failed with exception: ", e);

            final var newInstance = WorldImpl.constructResource(resourceClass);
            this.world.registerResource(newInstance);
            return newInstance;
        }
    }

    @Override
    public <TResource extends ProvidedResource> void provideResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        if (LOG_PROVIDE) {
            LOG.warn("provideResource called!");
        }

        try {
            this.world.registerResource(resourceClass, resource);
        } catch (final Throwable ignored) {
        }
    }

    @Override
    public <TResource extends ProvidedResource> TResource getResource(
            final Class<TResource> resourceType
    ) {
        if (LOG_GET) {
            LOG.warn("getResource called!");
        }

        return this.world.fetchResource(resourceType);
    }

    @Override
    public <TResource extends Resource> void createOrReplaceResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        throw new UnsupportedOperationException();
    }

    private class EntityManagerImpl implements EntityManager {
        @Override
        public Stream<Entity> getAllEntities() {
            return null;
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
}
