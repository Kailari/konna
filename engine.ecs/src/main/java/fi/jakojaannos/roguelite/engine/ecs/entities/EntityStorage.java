package fi.jakojaannos.roguelite.engine.ecs.entities;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import fi.jakojaannos.roguelite.engine.utilities.IdSupplier;

/**
 * Internal entity storage for default {@link fi.jakojaannos.roguelite.engine.ecs.EntityManager EntityManager}
 * implementation.
 */
public class EntityStorage {
    private final IdSupplier idSupplier = new IdSupplier();
    @Getter(AccessLevel.PACKAGE) private int capacity;
    private EntityImpl[] entities;
    private int entityCount;

    public EntityStorage(final int capacity) {
        this.capacity = 0;
        this.entityCount = 0;
        this.entities = new EntityImpl[0];
        resize(capacity);
    }

    EntityImpl create(final int maxComponentTypes) {
        final var entityId = this.idSupplier.get();
        this.entityCount += 1;
        return new EntityImpl(entityId, maxComponentTypes);
    }

    void spawn(final EntityImpl entity) {
        this.entities[entity.getId()] = entity;
    }

    void remove(final EntityImpl entity) {
        this.entityCount -= 1;
        this.idSupplier.free(entity.getId());
        this.entities[entity.getId()] = null;
    }

    void resize(final int capacity) {
        this.capacity = capacity;
        this.entities = Arrays.copyOf(this.entities, this.capacity);
    }

    public Stream<EntityImpl> stream() {
        return Arrays.stream(this.entities).filter(Objects::nonNull);
    }

    public boolean isFull() {
        return this.entityCount >= this.capacity;
    }

    public int count() {
        return this.entityCount;
    }
}
