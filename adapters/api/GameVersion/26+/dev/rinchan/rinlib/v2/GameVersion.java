package dev.rinchan.rinlib.v2;

import net.minecraft.SharedConstants;
public final class GameVersion {
    private GameVersion() {}
    public static int dataVersion() { return SharedConstants.getCurrentVersion().dataVersion().version(); }
}
