package dev.rinchan.rinlib.minecraft;

import net.minecraft.server.level.ServerPlayer;

/** @deprecated Import {@code dev.rinchan.rinlib.api.MobEffectState} instead. */
@Deprecated
public final class MobEffectState {
    private MobEffectState() {}

    public static boolean add(ServerPlayer player, String effectId, int durationTicks, int amplifier) {
        return dev.rinchan.rinlib.api.MobEffectState.add(player, effectId, durationTicks, amplifier);
    }
}
