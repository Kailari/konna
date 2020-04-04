package fi.jakojaannos.roguelite.engine.ecs.newimpl.world;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import fi.jakojaannos.roguelite.engine.ecs.newimpl.EntityHandle;
import fi.jakojaannos.roguelite.engine.ecs.newimpl.components.ComponentStorage;

public class FutureEntityHandle implements EntityHandle {
    private static final Logger LOG = LoggerFactory.getLogger(FutureEntityHandle.class);

    private final int idEstimate;
    private final Map<Class<?>, Object> components = new HashMap<>();
    private boolean destroyed;

    /**
     * Returns a guess for the upcoming ID of this entity. This might not reflect the final actual value, as entity
     * removal might shuffle IDs around a bit.
     *
     * @return guess of the ID for this entity
     */
    @Override
    public int getId() {
        return this.idEstimate;
    }

    public FutureEntityHandle(final int idEstimate) {
        this.idEstimate = idEstimate;
    }

    @Override
    public <TComponent> boolean addComponent(final TComponent component) {
        if (this.components.containsKey(component.getClass())) {
            LOG.warn("Tried adding same component type multiple times to a newly created entity!");
            return false;
        }

        this.components.put(component.getClass(), component);
        return true;
    }

    @Override
    public <TComponent> boolean removeComponent(final Class<TComponent> componentClass) {
        LOG.warn("Removed component from a newly created entity handle! Are you adding and then immediately removing "
                 + "a component?");
        return this.components.remove(componentClass) != null;
    }

    @Override
    public <TComponent> boolean hasComponent(final Class<TComponent> componentClass) {
        LOG.warn("Queried \"has component\" for a newly created entity handle! Component types should be known when "
                 + "the entity has just been created!");
        return this.components.containsKey(componentClass);
    }

    @Override
    public <TComponent> Optional<TComponent> getComponent(final Class<TComponent> componentClass) {
        return Optional.empty();
    }

    @Override
    public void destroy() {
        LOG.warn("Destroyed a newly created entity. Creating the entity should be avoided if it is going to be "
                 + "destroyed immediately within the same system.");
        this.destroyed = true;
    }

    public void create(final int id, final ComponentStorage componentStorage) {
        LOG.debug("Creating FutureEntityHandle with id {}", id);
        this.components.values()
                       .forEach((component) -> componentStorage.add(id, component));
    }
}
