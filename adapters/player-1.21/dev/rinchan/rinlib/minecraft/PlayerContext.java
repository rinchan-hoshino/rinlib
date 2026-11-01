package dev.rinchan.rinlib.minecraft;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/** @deprecated Import {@code dev.rinchan.rinlib.api.PlayerContext} instead. */
@Deprecated
public final class PlayerContext {
    private PlayerContext() {}

    public static String name(ServerPlayer player) {
        return dev.rinchan.rinlib.api.PlayerContext.name(player);
    }

    public static CommandSourceStack functionSource(ServerPlayer player) {
        return dev.rinchan.rinlib.api.PlayerContext.functionSource(player);
    }
}
