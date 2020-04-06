package fi.jakojaannos.roguelite.engine.ecs.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fi.jakojaannos.roguelite.engine.ecs.Component;
import fi.jakojaannos.roguelite.engine.ecs.Entity;
import fi.jakojaannos.roguelite.engine.ecs.EntityManager;
import fi.jakojaannos.roguelite.engine.ecs.newecs.World;
import fi.jakojaannos.roguelite.engine.ecs.newecs.world.LegacyCompat;

import static org.junit.jupiter.api.Assertions.*;

class EntityManagerTest {
    private EntityManager entityManager;

    @BeforeEach
    void beforeEach() {
        entityManager = new LegacyCompat(World.createNew()).getEntityManager();
    }

    @Test
    void entitiesCreatedWithCreateEntityHaveValidIDs() {
        Entity entity = entityManager.createEntity();
        assertFalse(entity.isMarkedForRemoval());
        assertTrue(entity.getId() >= 0);
        assertNotEquals(entity.getId(), entityManager.createEntity().getId());
    }

    @Test
    void createEntityCanCreateALargeNumberOfEntitiesWithoutThrowing() {
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 10000; ++i) {
                entityManager.createEntity();
            }
        });
    }

    @Test
    void addingComponentsToNonAppliedEntitiesWorks() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        final var added = entityManager.addComponentTo(entity, component);
        assertEquals(component, added);
    }

    @Test
    void gettingComponentsOfNonAppliedEntitiesWorks() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        final var added = entityManager.addComponentTo(entity, component);
        assertEquals(component, added);
    }

    @Test
    void removingComponentsOfNonAppliedEntitiesWorks() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);
        entityManager.removeComponentFrom(entity, component);

        assertTrue(entityManager.getComponentOf(entity, ComponentA.class).isEmpty());
    }

    @Test
    void getEntitiesWithDoesNotReturnNonAppliedEntities_SingleParameter() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        assertTrue(entityManager.getEntitiesWith(ComponentA.class)
                                .noneMatch(c -> c.component().equals(component)));
    }

    @Test
    void checkingComponentsOfNonAppliedEntitiesWorks() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        assertTrue(entityManager.hasComponent(entity, ComponentA.class));
    }

    @Test
    void callingDestroyEntityForNonAppliedEntityMarksItForRemoval() {
        Entity entity = entityManager.createEntity();
        entityManager.destroyEntity(entity);

        assertTrue(entity.isMarkedForRemoval());
    }

    @Test
    void destroyEntityMarksAnEntityForRemoval() {
        Entity entity = entityManager.createEntity();
        entityManager.applyModifications();
        entityManager.destroyEntity(entity);

        assertTrue(entity.isMarkedForRemoval());
    }

    @Test
    void getEntitiesWithReturnsAllExpectedEntities_SingleParameter() {
        Entity entityA = entityManager.createEntity();
        Entity entityB = entityManager.createEntity();
        Entity entityC = entityManager.createEntity();
        Entity entityD = entityManager.createEntity();
        entityManager.addComponentTo(entityA, new ComponentA());
        entityManager.addComponentTo(entityB, new ComponentA());
        entityManager.addComponentTo(entityB, new ComponentB());
        entityManager.addComponentTo(entityC, new ComponentC());
        entityManager.addComponentTo(entityD, new ComponentD());
        entityManager.applyModifications();

        assertTrue(entityManager.getEntitiesWith(ComponentA.class)
                                .anyMatch(e -> e.entity().getId() == entityA.getId()));
        assertTrue(entityManager.getEntitiesWith(ComponentA.class)
                                .anyMatch(e -> e.entity().getId() == entityB.getId()));
    }

    @Test
    void addComponentIfAbsentReturnsAddedComponentIfComponentIsAdded() {
        Entity entity = entityManager.createEntity();

        ComponentA newComponent = new ComponentA();
        assertEquals(newComponent, entityManager.addComponentIfAbsent(entity, ComponentA.class, () -> newComponent));
    }

    @Test
    void addComponentIfAbsentDoesNotReplaceTheComponent() {
        Entity entity = entityManager.createEntity();
        ComponentA component = new ComponentA();
        entityManager.addComponentTo(entity, component);

        final var added = entityManager.addComponentIfAbsent(entity, ComponentA.class, ComponentA::new);
        assertEquals(component, added);
    }

    @Test
    void removeComponentIfPresentReturnsFalseIfComponentIsNotPresent() {
        Entity entity = entityManager.createEntity();

        assertFalse(entityManager.removeComponentIfPresent(entity, ComponentA.class));
    }

    @Test
    void removeComponentIfPresentReturnsTrueIfComponentIsPresent() {
        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new ComponentA());

        assertTrue(entityManager.removeComponentIfPresent(entity, ComponentA.class));
    }

    @Test
    void removeComponentIfPresentRemovesTheComponentIfComponentIsPresent() {
        Entity entity = entityManager.createEntity();
        entityManager.addComponentTo(entity, new ComponentA());

        entityManager.removeComponentIfPresent(entity, ComponentA.class);
        assertFalse(entityManager.hasComponent(entity, ComponentA.class));
    }

    private static class ComponentA implements Component {
    }

    private static class ComponentB implements Component {
    }

    private static class ComponentC implements Component {
    }

    private static class ComponentD implements Component {
    }
}
