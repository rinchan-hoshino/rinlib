package dev.rinchan.rinlib.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

/** Player identity and operator-level function sources across Minecraft versions. */
public final class PlayerContext {
    private PlayerContext() {}

    public static String name(ServerPlayer player) {
        return player.getGameProfile().name();
    }

    public static CommandSourceStack functionSource(ServerPlayer player) {
        return player.createCommandSourceStack().withPermission(LevelBasedPermissionSet.GAMEMASTER).withSuppressedOutput();
    }
}
