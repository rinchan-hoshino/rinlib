package dev.rinchan.rinlib.minecraft;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

/** @deprecated 改用 dev.rinchan.rinlib.api.PlayerContext */
@Deprecated(forRemoval = true)
public final class PlayerContext {
    private PlayerContext() {}

    public static String name(ServerPlayer player) {
        return dev.rinchan.rinlib.api.PlayerContext.name(player);
    }

    public static CommandSourceStack functionSource(ServerPlayer player) {
        return dev.rinchan.rinlib.api.PlayerContext.functionSource(player);
    }
}
