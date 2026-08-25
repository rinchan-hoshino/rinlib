package dev.rinchan.rinlib.minecraft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

public final class MobEffectState {
    private MobEffectState() {
    }

    public static boolean add(ServerPlayer player, String effectId, int durationTicks, int amplifier) {
        ResourceLocation id = ResourceLocation.tryParse(effectId);
        if (id == null) {
            return false;
        }
        return BuiltInRegistries.MOB_EFFECT.get(id)
                .map(effect -> player.addEffect(new MobEffectInstance(effect, durationTicks, amplifier)))
                .orElse(false);
    }
}
