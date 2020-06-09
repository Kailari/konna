package fi.jakojaannos.roguelite.engine.utilities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IdSupplierTest {
    @Test
    void firstGetReturnsZero() {
        IdSupplier supplier = new IdSupplier();
        assertEquals(0, supplier.get());
    }

    @Test
    void getSuppliesIdsInOrder() {
        IdSupplier supplier = new IdSupplier();
        for (int i = 0; i <= 100; ++i) {
            assertEquals(i, supplier.get());
        }
    }

    @Test
    void getSuppliesFreedIdsBeforeCreatingNew() {
        IdSupplier supplier = new IdSupplier();
        for (int i = 0; i <= 100; ++i) {
            supplier.get();
        }

        supplier.free(42);
        assertEquals(42, supplier.get());
    }

    @Test
    void getSuppliesFreedIdsInAscendingOrder() {
        IdSupplier supplier = new IdSupplier();
        for (int i = 0; i <= 100; ++i) {
            supplier.get();
        }

        supplier.free(42);
        supplier.free(45);
        supplier.free(44);
        supplier.free(20);
        supplier.free(43);
        supplier.free(40);
        supplier.free(41);

        assertEquals(20, supplier.get());
        assertEquals(40, supplier.get());
        assertEquals(41, supplier.get());
        assertEquals(42, supplier.get());
        assertEquals(43, supplier.get());
        assertEquals(44, supplier.get());
        assertEquals(45, supplier.get());
    }

    @Test
    void getSuppliesCorrectIdsAfterFreedIds() {
        IdSupplier supplier = new IdSupplier();
        for (int i = 0; i <= 100; ++i) {
            supplier.get();
        }

        supplier.free(42);
        supplier.free(45);
        supplier.free(44);
        supplier.free(20);
        supplier.free(43);
        supplier.free(40);
        supplier.free(41);
        supplier.get();
        supplier.get();
        supplier.get();
        supplier.get();
        supplier.get();
        supplier.get();
        supplier.get();

        assertEquals(101, supplier.get());
        assertEquals(102, supplier.get());
        assertEquals(103, supplier.get());
        assertEquals(104, supplier.get());
        assertEquals(105, supplier.get());
    }
}
