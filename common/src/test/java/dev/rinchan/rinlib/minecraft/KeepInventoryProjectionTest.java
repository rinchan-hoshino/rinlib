package dev.rinchan.rinlib.minecraft;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class KeepInventoryProjectionTest {
    @Test
    void projectsOnlyInsideNestedScopes() {
        assertFalse(KeepInventoryProjection.isActive());
        try (var outer = KeepInventoryProjection.enter()) {
            assertTrue(KeepInventoryProjection.isActive());
            try (var inner = KeepInventoryProjection.enter()) {
                assertTrue(KeepInventoryProjection.isActive());
            }
            assertTrue(KeepInventoryProjection.isActive());
        }
        assertFalse(KeepInventoryProjection.isActive());
    }
}
