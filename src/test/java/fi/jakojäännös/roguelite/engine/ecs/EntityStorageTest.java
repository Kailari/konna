package fi.jakojäännös.roguelite.engine.ecs;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EntityStorageTest {
    @Test
    void eachEntityCreatedHasUniqueId() {
        EntityStorage storage = new EntityStorage(256);
        assertNotEquals(storage.create(100).getId(),
                        storage.create(100).getId());
    }

    @Test
    void streamContainsAllSpawned() {
        EntityStorage storage = new EntityStorage(256);
        List<Entity> entities = List.of(
                storage.create(10),
                storage.create(10),
                storage.create(10),
                storage.create(10),
                storage.create(10));

        entities.forEach(storage::spawn);
        assertTrue(storage.stream().allMatch(entities::contains));
    }

    @Test
    void streamDoesNotContainCreatedButNotYetSpawned() {
        EntityStorage storage = new EntityStorage(256);
        Entity created = storage.create(10);
        Entity spawned = storage.create(10);
        storage.spawn(spawned);

        assertEquals(0, storage.stream().filter(e -> created.getId() == e.getId()).count());
        assertEquals(1, storage.stream().filter(e -> spawned.getId() == e.getId()).count());
    }

    @Test
    void streamDoesNotContainRemoved() {
        EntityStorage storage = new EntityStorage(256);
        Entity removed = storage.create(10);
        Entity spawned = storage.create(10);
        storage.spawn(removed);
        storage.spawn(spawned);

        storage.remove(removed);

        assertEquals(0, storage.stream().filter(e -> removed.getId() == e.getId()).count());
        assertEquals(1, storage.stream().filter(e -> spawned.getId() == e.getId()).count());
    }

    @Test
    void spawnThrowsIfEntityIsNull() {
        assertThrows(NullPointerException.class, () -> new EntityStorage(100).spawn(null));
    }

    @Test
    void removeThrowsIfEntityIsNull() {
        assertThrows(NullPointerException.class, () -> new EntityStorage(100).remove(null));
    }
}
