package fi.jakojaannos.roguelite.engine.ecs.world;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.ProvidedResource;
import fi.jakojaannos.roguelite.engine.ecs.Resource;
import fi.jakojaannos.roguelite.engine.ecs.World;

public class WorldImpl implements World {
    private final EntityManager entityManager;
    private final Map<Class<? extends Resource>, Resource> resourceStorage = new HashMap<>();
    private final Map<Class<? extends ProvidedResource>, ProvidedResource> providedResourceStorage = new HashMap<>();

    @Override
    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public WorldImpl(final EntityManager entityManager) {
        this.entityManager = entityManager;
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
    public <TResource extends Resource> TResource getOrCreateResource(final Class<TResource> resourceClass) {
        return (TResource) this.resourceStorage.computeIfAbsent(resourceClass, WorldImpl::constructResource);
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

    public static  <TResource extends Resource> TResource constructResource(
            final Class<? extends TResource> resourceClass
    ) {
        try {
            return resourceClass.getConstructor().newInstance();
        } catch (final InstantiationException e) {
            throw new IllegalStateException(String.format(
                    "Resource type %s represents an abstract class!",
                    resourceClass.getSimpleName()
            ), e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(String.format(
                    "Resource type %s default constructor is not accessible!",
                    resourceClass.getSimpleName()
            ), e);
        } catch (final InvocationTargetException e) {
            throw new IllegalStateException(String.format(
                    "Error creating resource of type %s, constructor threw an exception",
                    resourceClass.getSimpleName()
            ), e);
        } catch (final NoSuchMethodException e) {
            throw new IllegalStateException(String.format(
                    "Resource type %s does not define a default constructor!",
                    resourceClass.getSimpleName()
            ));
        }
    }
}
