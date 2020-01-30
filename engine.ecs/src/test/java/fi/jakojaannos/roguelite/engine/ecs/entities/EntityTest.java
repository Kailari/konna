package fi.jakojaannos.roguelite.engine.ecs.entities;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EntityTest {
    @ParameterizedTest
    @CsvSource({"0,0", "1,1", "8,1", "9,2", "100,13"})
    void constructorCreatesMaskWithCorrectSize(int nComponentTypes, int maskSize) {
        assertEquals(maskSize, new EntityImpl(0, nComponentTypes).getComponentBitmask().length);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 69, 1337, Integer.MAX_VALUE})
    void idGetterReturnsIdProvidedInConstructor(int id) {
        assertEquals(id, new EntityImpl(id, 1).getId());
    }

    @Test
    void entitiesAreNotInitiallyMarkedForRemoval() {
        assertFalse(new EntityImpl(0, 1).isMarkedForRemoval());
    }

    @Test
    void isMarkedForRemovalReturnsTrueAfterMarkingForRemoval() {
        EntityImpl entity = new EntityImpl(0, 1);
        entity.markForRemoval();
        assertTrue(entity.isMarkedForRemoval());
    }
}
