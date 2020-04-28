package fi.jakojaannos.roguelite.engine.ecs.world.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.ecs.EntityDataHandle;

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
