package dev.rinchan.rinlib.v2;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EventTest {
    @Test void listenersRunInRegistrationOrder() {
        var event = new Event<Runnable>();
        var calls = new ArrayList<Integer>();
        event.register(() -> calls.add(1));
        event.register(() -> calls.add(2));
        event.listeners().forEach(Runnable::run);
        assertEquals(List.of(1, 2), calls);
    }
    @Test void nullListenersAreRejected() {
        assertThrows(NullPointerException.class, () -> new Event<Runnable>().register(null));
    }
}
