package dev.rinchan.rinlib.state;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SharedOwnerStateTest {
    @Test
    void readsAndWritesOnlyTheFixedSharedOwner() {
        UUID legacyOwner = UUID.randomUUID();
        Object legacyValue = new Object();
        Object sharedValue = new Object();
        Map<UUID, Object> values = new HashMap<>();
        values.put(legacyOwner, legacyValue);

        assertEquals(new UUID(0L, 1L), SharedOwnerState.OWNER);
        assertFalse(SharedOwnerState.contains(values));
        assertNull(SharedOwnerState.get(values));

        assertSame(sharedValue, SharedOwnerState.put(values, sharedValue));
        assertTrue(SharedOwnerState.contains(values));
        assertSame(sharedValue, SharedOwnerState.get(values));
        assertSame(legacyValue, values.get(legacyOwner));
        assertEquals(2, values.size());
    }
}
