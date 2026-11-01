package dev.rinchan.rinlib.minecraft;

import net.minecraft.server.level.ServerPlayer;

/** @deprecated 改用 dev.rinchan.rinlib.api.MobEffectState */
@Deprecated(forRemoval = true)
public final class MobEffectState {
    private MobEffectState() {}

    public static boolean add(ServerPlayer player, String effectId, int durationTicks, int amplifier) {
        return dev.rinchan.rinlib.api.MobEffectState.add(player, effectId, durationTicks, amplifier);
    }
}
