package dev.rinchan.rinlib.state;

/** @deprecated Use {@code current || update} at the call site. */
@Deprecated
public final class StickyBoolean {
    private StickyBoolean() {
    }

    public static boolean next(boolean current, boolean update) {
        return current || update;
    }
}
