package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;
import fi.jakojaannos.roguelite.engine.ecs.EntityHandle;

import static org.junit.jupiter.api.Assertions.*;

public class EntityStorageTest {
    private EntityStorage storage;

    @BeforeEach
    void beforeEach() {
        storage = new EntityStorage();

        for (int i = 0; i < 100; i++) {
            storage.createEntity(new ComponentA());
            storage.createEntity(new ComponentB());

            storage.createEntity(new ComponentA(),
                                 new ComponentB());
        }
        storage.commitModifications();
    }

    @Test
    void creatingEntityWithoutComponentsReturnsValidHandle() {
        final var handle = storage.createEntity();
        assertNotNull(handle);
        assertFalse(handle.isDestroyed());
        assertFalse(handle.isPendingRemoval());
    }

    @Test
    void creatingMultipleEntitiesReturnsUniqueHandleEveryTime() {
        final var handleA = storage.createEntity();
        final var handleB = storage.createEntity();
        final var handleC = storage.createEntity();

        assertNotEquals(handleA, handleB);
        assertNotEquals(handleB, handleC);
    }

    @Test
    void createdEntityHasAllComponentsSuppliedToCreateEntity() {
        final var handle = storage.createEntity(new ComponentA(),
                                                new ComponentB());
        assertAll(
                () -> assertTrue(handle.hasComponent(ComponentA.class)),
                () -> assertTrue(handle.hasComponent(ComponentB.class))
        );
    }

    @Test
    void createdEntityDoesNotHaveComponentNotSuppliedToCreateEntity() {
        final var handle = storage.createEntity(new ComponentA(),
                                                new ComponentB());
        assertFalse(handle.hasComponent(ComponentC.class));
    }

    @Test
    void createdEntityDoesNotAppearOnTheStreamWhenChangesHaveNotYetBeenCommitted() {
        storage.createEntity(new ComponentA(), new ComponentB());
        final var stream = storage.stream(new Class[]{ComponentA.class},
                                          new boolean[]{false},
                                          new boolean[]{false},
                                          objects -> new Object(),
                                          false);
        assertEquals(200, stream.count());
    }

    @Test
    void createdEntityIsOnTheStreamAfterChangesHaveBeenCommitted() {
        storage.createEntity(new ComponentA(), new ComponentB());
        storage.commitModifications();

        final var stream = createStreamOf(ComponentA.class);
        assertEquals(201, stream.count());
    }

    @Test
    void justCreatedEntityWithManuallyAddedComponentIsOnTheStreamAfterCommit() {
        final var handle = storage.createEntity(new ComponentA());
        handle.addComponent(new ComponentB());

        storage.commitModifications();

        final var streamA = createStreamOf(ComponentA.class);
        final var streamB = createStreamOf(ComponentB.class);
        assertEquals(201, streamA.count());
        assertEquals(201, streamB.count());
    }

    @Test
    void justCreatedEntityWithManuallyRemovedComponentIsOnTheCorrectStreamsAfterCommit() {
        final var handle = storage.createEntity(new ComponentA(), new ComponentB());
        handle.removeComponent(ComponentB.class);

        storage.commitModifications();

        final var streamA = createStreamOf(ComponentA.class);
        final var streamB = createStreamOf(ComponentB.class);
        assertEquals(201, streamA.count());
        assertEquals(200, streamB.count());
    }

    @Test
    void justCreatedEntityWithMultipleManuallyAddedComponentsIsOnTheStreamAfterCommit() {
        final var handle = storage.createEntity(new ComponentA());
        handle.addComponent(new ComponentB());
        handle.addComponent(new ComponentC());
        handle.addComponent(new ComponentD());
        handle.addComponent(new ComponentE());

        storage.commitModifications();

        final var streamA = createStreamOf(ComponentA.class);
        final var streamB = createStreamOf(ComponentB.class);
        final var streamC = createStreamOf(ComponentC.class);
        final var streamD = createStreamOf(ComponentD.class);
        final var streamE = createStreamOf(ComponentE.class);
        assertEquals(201, streamA.count());
        assertEquals(201, streamB.count());
        assertEquals(1, streamC.count());
        assertEquals(1, streamD.count());
        assertEquals(1, streamE.count());
    }

    @Test
    void entityWithComponentsAddedOverMultiplePassesIsOnStream() {
        final var handle = storage.createEntity(new ComponentA());
        handle.addComponent(new ComponentB());
        storage.commitModifications();

        handle.addComponent(new ComponentC());
        storage.commitModifications();

        handle.addComponent(new ComponentD());
        handle.addComponent(new ComponentE());
        storage.commitModifications();

        final var streamA = createStreamOf(ComponentA.class);
        final var streamB = createStreamOf(ComponentB.class);
        final var streamC = createStreamOf(ComponentC.class);
        final var streamD = createStreamOf(ComponentD.class);
        final var streamE = createStreamOf(ComponentE.class);
        assertEquals(201, streamA.count());
        assertEquals(201, streamB.count());
        assertEquals(1, streamC.count());
        assertEquals(1, streamD.count());
        assertEquals(1, streamE.count());
    }

    @Test
    void entityWithComponentsAddedAndRemovedOverMultiplePassesIsOnStream() {
        final var handle = storage.createEntity(new ComponentA());
        handle.addComponent(new ComponentB());
        storage.commitModifications();

        handle.addComponent(new ComponentC());
        handle.removeComponent(ComponentA.class);
        storage.commitModifications();

        handle.addComponent(new ComponentD());
        handle.addComponent(new ComponentE());
        storage.commitModifications();

        final var streamA = createStreamOf(ComponentA.class);
        final var streamB = createStreamOf(ComponentB.class);
        final var streamC = createStreamOf(ComponentC.class);
        final var streamD = createStreamOf(ComponentD.class);
        final var streamE = createStreamOf(ComponentE.class);
        assertEquals(200, streamA.count());
        assertEquals(201, streamB.count());
        assertEquals(1, streamC.count());
        assertEquals(1, streamD.count());
        assertEquals(1, streamE.count());
    }

    @Test
    void streamContainsSomeEntityWithCorrectComponent() {
        final var a = new ComponentA();
        final var b = new ComponentB();
        final var c = new ComponentC();
        final var d = new ComponentD();
        final var e = new ComponentE();

        final var handle = storage.createEntity();
        handle.addComponent(b);
        storage.commitModifications();

        handle.addComponent(c);
        handle.removeComponent(ComponentA.class);
        storage.commitModifications();

        handle.addComponent(d);
        handle.addComponent(e);
        storage.commitModifications();

        final var streamA = createStreamOf(ComponentA.class);
        final var streamB = createStreamOf(ComponentB.class);
        final var streamC = createStreamOf(ComponentC.class);
        final var streamD = createStreamOf(ComponentD.class);
        final var streamE = createStreamOf(ComponentE.class);
        assertEquals(0, streamA.filter(h -> h.getComponent(ComponentA.class).orElseThrow().equals(a)).count());
        assertEquals(1, streamB.filter(h -> h.getComponent(ComponentB.class).orElseThrow().equals(b)).count());
        assertEquals(1, streamC.filter(h -> h.getComponent(ComponentC.class).orElseThrow().equals(c)).count());
        assertEquals(1, streamD.filter(h -> h.getComponent(ComponentD.class).orElseThrow().equals(d)).count());
        assertEquals(1, streamE.filter(h -> h.getComponent(ComponentE.class).orElseThrow().equals(e)).count());
    }

    @Test
    void streamContainsExactlyOneEntityWithAllCorrectComponents() {
        final var a = new ComponentA();
        final var b = new ComponentB();
        final var c = new ComponentC();
        final var d = new ComponentD();
        final var e = new ComponentE();

        final var handle = storage.createEntity(a);
        handle.addComponent(b);
        storage.commitModifications();

        handle.addComponent(c);
        handle.removeComponent(ComponentA.class);
        storage.commitModifications();

        handle.addComponent(d);
        handle.addComponent(e);
        storage.commitModifications();

        final var stream = createStreamOf(ComponentB.class, ComponentC.class, ComponentD.class, ComponentE.class);
        assertEquals(1, stream.filter(h -> h.getComponent(ComponentB.class).orElseThrow().equals(b)
                                           && h.getComponent(ComponentC.class).orElseThrow().equals(c)
                                           && h.getComponent(ComponentD.class).orElseThrow().equals(d)
                                           && h.getComponent(ComponentE.class).orElseThrow().equals(e))
                              .count());
    }

    @Test
    void creatingAndDestroyingLotsOfEntitiesWorks() {
        final var count = createStreamOf(ComponentA.class, ComponentB.class).count();

        for (int i = 0; i < 100; i++) {
            storage.createEntity(new ComponentB(), new ComponentA());
            final var handle = storage.createEntity(new ComponentB(), new ComponentA());
            storage.commitModifications();
            handle.destroy();

            storage.commitModifications();
        }

        final var result = createStreamOf(ComponentA.class, ComponentB.class).count();
        assertEquals(count + 100, result);
    }

    @Test
    void destroyingAllButOneEntitiesDoesNotTouchTheOneEntityWhenTheOneIsSomewhereInTheMiddle() {
        final var handles = new EntityHandle[100];
        ComponentE e = null;
        for (int i = 0; i < 100; i++) {
            final var component = new ComponentE();
            if (i == 50) {
                e = component;
            }
            handles[i] = storage.createEntity(component);
        }
        storage.commitModifications();

        for (int i = 0; i < 100; i++) {
            if (i == 50) {
                ++i;
            }
            handles[i].destroy();
        }
        storage.commitModifications();

        assertEquals(e, handles[50].getComponent(ComponentE.class)
                                   .orElseThrow());
    }

    @Test
    void destroyingAllButOneEntitiesDoesNotTouchTheOneEntityWhenItIsLast() {
        final var handles = new EntityHandle[100];
        ComponentE e = null;
        for (int i = 0; i < 100; i++) {
            final var component = new ComponentE();
            if (i == 99) {
                e = component;
            }
            handles[i] = storage.createEntity(component);
        }
        storage.commitModifications();

        for (int i = 0; i < 99; i++) {
            handles[i].destroy();
        }
        storage.commitModifications();

        assertEquals(e, handles[99].getComponent(ComponentE.class)
                                   .orElseThrow());
    }

    @Test
    void destroyingAllButOneEntitiesDoesNotTouchTheOneEntityWhenItIsFirst() {
        final var handles = new EntityHandle[100];
        ComponentE e = null;
        for (int i = 0; i < 100; i++) {
            final var component = new ComponentE();
            if (i == 0) {
                e = component;
            }
            handles[i] = storage.createEntity(component);
        }
        storage.commitModifications();

        for (int i = 0; i < 100; i++) {
            if (i == 0) {
                ++i;
            }
            handles[i].destroy();
        }
        storage.commitModifications();

        assertEquals(e, handles[0].getComponent(ComponentE.class)
                                  .orElseThrow());
    }

    @Test
    void sequentialStreamContainsCorrectNumberOfElementsWhenSuchEntitiesExist() {
        final var stream = createStreamOf(ComponentA.class);
        assertEquals(200, stream.count());
    }

    @Test
    void parallelStreamContainsCorrectNumberOfElementsWhenSuchEntitiesExist() {
        final var stream = createStreamOf(ComponentB.class);
        assertEquals(200, stream.count());
    }

    @Test
    void allElementsOnStreamHaveTheRequiredComponents() {
        final var stream = createStreamOf(ComponentA.class, ComponentB.class);
        assertTrue(stream.allMatch(handle -> handle.hasComponent(ComponentA.class)
                                             && handle.hasComponent(ComponentB.class)));
    }

    @Test
    void streamContainsZeroElementsWhenSuchEntitiesDoNotExist() {
        final var stream = storage.stream(new Class[]{ComponentC.class},
                                          new boolean[]{false},
                                          new boolean[]{false},
                                          objects -> new Object(),
                                          false);
        assertEquals(0, stream.count());
    }

    @Test
    void entitiesWithExcludedComponentsAreNotOnTheStream() {
        final var stream = storage.stream(new Class[]{ComponentA.class, ComponentB.class},
                                          new boolean[]{false, true},
                                          new boolean[]{false, false},
                                          objects -> new Object(),
                                          false);
        assertEquals(100, stream.count());
    }

    @Test
    void creatingMultipleEntitiesAndAddingAndRemovingComponentsResultsInCorrectNumberOfEntities() {
        for (int i = 0; i < 20; i++) {
            storage.createEntity(new ComponentB(), new ComponentA());
            final var handle = storage.createEntity(new ComponentB(), new ComponentA());
            handle.removeComponent(ComponentB.class);

            storage.createEntity(new ComponentB(), new ComponentA(), new ComponentC());
        }

        storage.commitModifications();

        final var streamA = createStreamOf(ComponentA.class);
        final var streamB = createStreamOf(ComponentB.class);
        final var streamC = createStreamOf(ComponentC.class);
        assertEquals(260, streamA.count());
        assertEquals(240, streamB.count());
        assertEquals(20, streamC.count());
    }

    @Test
    void creatingMultipleEntitiesAndAddingAndRemovingComponentsResultsInExactCorrectInstances() {
        final var componentA1 = new ComponentA();
        final var componentA3 = new ComponentA();

        final var componentB1 = new ComponentB();
        final var componentB2 = new ComponentB();
        final var componentB3 = new ComponentB();

        final var componentC1 = new ComponentC();
        final var componentC2 = new ComponentC();
        final var componentC3 = new ComponentC();

        final var entity1 = storage.createEntity();
        final var entity2 = storage.createEntity();
        final var entity3 = storage.createEntity();

        // "random" component adds/removes
        entity1.addComponent(componentA1);
        entity2.addComponent(new ComponentA());
        entity2.addComponent(componentB2);
        entity2.addComponent(componentC2);
        entity1.addComponent(componentB1);
        entity3.addComponent(componentB3);
        entity2.removeComponent(ComponentA.class);
        entity1.addComponent(componentC1);
        entity3.addComponent(componentA3);
        entity3.addComponent(componentC3);

        storage.commitModifications();
        assertEquals(componentA1, entity1.getComponent(ComponentA.class).orElseThrow());
        assertTrue(entity2.getComponent(ComponentA.class).isEmpty());
        assertEquals(componentA3, entity3.getComponent(ComponentA.class).orElseThrow());

        assertEquals(componentB1, entity1.getComponent(ComponentB.class).orElseThrow());
        assertEquals(componentB2, entity2.getComponent(ComponentB.class).orElseThrow());
        assertEquals(componentB3, entity3.getComponent(ComponentB.class).orElseThrow());

        assertEquals(componentC1, entity1.getComponent(ComponentC.class).orElseThrow());
        assertEquals(componentC2, entity2.getComponent(ComponentC.class).orElseThrow());
        assertEquals(componentC3, entity3.getComponent(ComponentC.class).orElseThrow());
    }

    @Test
    void creatingMultipleEntitiesAndAddingAndRemovingComponentsResultsInExactCorrectInstances_removeNonExistentComponent() {
        final var componentA1 = new ComponentA();
        final var componentA3 = new ComponentA();

        final var componentB1 = new ComponentB();
        final var componentB2 = new ComponentB();
        final var componentB3 = new ComponentB();

        final var componentC1 = new ComponentC();
        final var componentC2 = new ComponentC();
        final var componentC3 = new ComponentC();

        final var entity1 = storage.createEntity();
        final var entity2 = storage.createEntity();
        final var entity3 = storage.createEntity();

        // "random" component adds/removes
        entity1.addComponent(componentA1);
        entity2.addComponent(componentB2);
        entity2.addComponent(componentC2);
        entity1.addComponent(componentB1);
        entity3.addComponent(componentB3);
        entity2.removeComponent(ComponentB.class);
        entity1.addComponent(componentC1);
        entity3.addComponent(componentA3);
        entity3.addComponent(componentC3);

        storage.commitModifications();
        assertEquals(componentA1, entity1.getComponent(ComponentA.class).orElseThrow());
        assertEquals(componentA3, entity3.getComponent(ComponentA.class).orElseThrow());

        assertEquals(componentB1, entity1.getComponent(ComponentB.class).orElseThrow());
        assertTrue(entity2.getComponent(ComponentB.class).isEmpty());
        assertEquals(componentB3, entity3.getComponent(ComponentB.class).orElseThrow());

        assertEquals(componentC1, entity1.getComponent(ComponentC.class).orElseThrow());
        assertEquals(componentC2, entity2.getComponent(ComponentC.class).orElseThrow());
        assertEquals(componentC3, entity3.getComponent(ComponentC.class).orElseThrow());
    }

    @SuppressWarnings("rawtypes")
    private Stream<EntityDataHandle<Object>> createStreamOf(final Class... components) {
        return storage.stream(components,
                              new boolean[components.length],
                              new boolean[components.length],
                              objects -> new Object(),
                              false);
    }

    private static final class ComponentA {}

    private static final class ComponentB {}

    private static final class ComponentC {}

    private static final class ComponentD {}

    private static final class ComponentE {}
}
