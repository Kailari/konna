package fi.jakojaannos.roguelite.engine.ecs.world;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.World;

@Slf4j
public class WorldImpl implements World {
    private final EntityManager entityManager;
    private final Map<Class<? extends Resource>, Resource> resourceStorage = new HashMap<>();
    private final Map<Class<? extends ProvidedResource>, ProvidedResource> providedResourceStorage = new HashMap<>();

    public WorldImpl(final EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Override
    public <TResource extends Resource> void createOrReplaceResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        this.resourceStorage.put(resourceClass, resource);
    }

    @Override
    public <TResource extends ProvidedResource> void provideResource(
            final Class<TResource> resourceClass,
            final TResource resource
    ) {
        this.providedResourceStorage.put(resourceClass, resource);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TResource extends Resource> TResource getOrCreateResource(final Class<TResource> resourceType) {
        return (TResource) this.resourceStorage.computeIfAbsent(resourceType,
                                                                rt -> constructResource(resourceType, rt));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <TResource extends ProvidedResource> TResource getResource(final Class<TResource> resourceType) {
        if (!this.providedResourceStorage.containsKey(resourceType)) {
            throw new IllegalStateException("Tried to get provided resource \"" + resourceType.getSimpleName() +
                                                    "\" but no instance exists!");
        }

        return (TResource) this.providedResourceStorage.get(resourceType);
    }

    private <TResource extends Resource> Resource constructResource(
            final Class<? extends TResource> resourceType,
            final Class<? extends Resource> rt
    ) {
        try {
            return resourceType.getConstructor().newInstance();
        } catch (final InstantiationException e) {
            throw new IllegalStateException(String.format(
                    "Resource type %s represents an abstract class!",
                    rt.getSimpleName()
            ), e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(String.format(
                    "Resource type %s default constructor is not accessible!",
                    rt.getSimpleName()
            ), e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(String.format(
                    "Error creating resource of type %s, constructor threw an exception",
                    rt.getSimpleName()
            ), e);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(String.format(
                    "Resource type %s does not define a default constructor!",
                    rt.getSimpleName()
            ));
        }
    }
}
