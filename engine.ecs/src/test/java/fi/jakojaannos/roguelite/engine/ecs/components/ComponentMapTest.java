package fi.jakojaannos.roguelite.engine.ecs.components;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class ComponentMapTest {
    private ComponentMap<MockComponent> storage;

    @BeforeEach
    void beforeEach() {
        storage = new ComponentMap<>(100, MockComponent.class);
    }

    @Test
    void addComponentAddsTheComponent() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        assertNotNull(storage.getComponent(entity));
    }

    @Test
    void removeComponentRemovesTheComponent() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);

        assertNull(storage.getComponent(entity));
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_addFirst() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);

        assertNull(storage.getComponent(entity));
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_removeFirst() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());

        assertNotNull(storage.getComponent(entity));
    }

    @Test
    void tasksAreAppliedInOrderTheyAreCalled_complex() {
        EntityImpl entity = new EntityImpl(0, 100);

        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());
        storage.removeComponent(entity);
        storage.addComponent(entity, new MockComponent());

        assertNotNull(storage.getComponent(entity));
    }

    public static class MockComponent implements Component {
    }
}
