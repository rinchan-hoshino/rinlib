package dev.rinchan.rinlib.state;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StickyBooleanTest {
    @Test
    void falseCanBecomeTrueButTrueNeverBecomesFalse() {
        assertFalse(StickyBoolean.next(false, false));
        assertTrue(StickyBoolean.next(false, true));
        assertTrue(StickyBoolean.next(true, false));
        assertTrue(StickyBoolean.next(true, true));
    }
}
