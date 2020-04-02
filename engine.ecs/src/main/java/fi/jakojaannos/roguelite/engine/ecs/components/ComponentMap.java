package fi.jakojaannos.roguelite.engine.ecs.components;

import java.lang.reflect.Array;
import java.util.Arrays;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;

class ComponentMap<TComponent extends Component> {
    private int entityCapacity;
    private TComponent[] components;

    ComponentMap(
            final int entityCapacity,
            final Class<TComponent> componentClass
    ) {
        this.entityCapacity = entityCapacity;
        // noinspection unchecked
        this.components = (TComponent[]) Array.newInstance(componentClass, this.entityCapacity);
    }

    void addComponent(final EntityImpl entity, final TComponent component) {
        this.components[entity.getId()] = component;
    }

    void removeComponent(final EntityImpl entity) {
        this.components[entity.getId()] = null;
    }

    TComponent getComponent(final EntityImpl entity) {
        return this.components[entity.getId()];
    }

    void resize(final int entityCapacity) {
        if (entityCapacity > this.entityCapacity) {
            this.entityCapacity = entityCapacity;
            this.components = Arrays.copyOf(this.components, this.entityCapacity);
        }
    }
}
