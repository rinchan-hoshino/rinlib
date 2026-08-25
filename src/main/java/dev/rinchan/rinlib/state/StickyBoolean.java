package dev.rinchan.rinlib.state;

/** A one-way boolean projection: false may become true, but true never regresses. */
public final class StickyBoolean {
    private StickyBoolean() {
    }

    public static boolean next(boolean current, boolean update) {
        return current || update;
    }
}
