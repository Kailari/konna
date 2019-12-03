package fi.jakojaannos.roguelite.engine.ecs.components;

import fi.jakojaannos.roguelite.engine.ecs.entities.EntityImpl;
import fi.jakojaannos.roguelite.test.mock.engine.ecs.MockComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

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
}
