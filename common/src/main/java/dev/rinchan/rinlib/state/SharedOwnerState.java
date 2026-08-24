package dev.rinchan.rinlib.state;

import java.util.Map;
import java.util.UUID;

/**
 * Owns the stable identity used when many actor-keyed views must resolve to one
 * shared value. Existing actor-keyed entries are deliberately left untouched.
 */
public final class SharedOwnerState {
    public static final UUID OWNER = new UUID(0L, 1L);

    private SharedOwnerState() {
    }

    public static boolean contains(Map<UUID, ?> values) {
        return values.containsKey(OWNER);
    }

    public static <V> V get(Map<UUID, V> values) {
        return values.get(OWNER);
    }

    public static <V> V put(Map<UUID, V> values, V value) {
        values.put(OWNER, value);
        return value;
    }

    public static <V> V remove(Map<UUID, V> values) {
        return values.remove(OWNER);
    }
}
