package dev.rinchan.rinlib.v2.net;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PresencePayloadTest {
    @Test void separatelyCreatedPayloadEncodesWithTheSameId() {
        var type = PresencePayload.type("test", "1");
        var payload = PresencePayload.of(PresencePayload.type("test", "1"));
        assertDoesNotThrow(() -> PresencePayload.codec(type).encode(null, payload));
        assertEquals(PresencePayload.of(type).hashCode(), payload.hashCode());
    }

    @Test void differentIdsAreRejectedByTheUnitCodec() {
        var type = PresencePayload.type("test", "1");
        for (var other : new String[]{"other", "test"}) {
            var payload = PresencePayload.of(PresencePayload.type(other, other.equals("test") ? "2" : "1"));
            assertNotEquals(PresencePayload.of(type), payload);
            assertThrows(IllegalStateException.class, () -> PresencePayload.codec(type).encode(null, payload));
        }
        assertNotEquals(PresencePayload.of(type), null);
        assertNotEquals(PresencePayload.of(type), "test:presence_v1");
    }
}
