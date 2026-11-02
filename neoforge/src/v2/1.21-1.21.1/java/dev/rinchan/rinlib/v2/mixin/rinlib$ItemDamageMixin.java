package dev.rinchan.rinlib.v2.mixin;


import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.rinchan.rinlib.v2.ItemEvents;

@Mixin(ItemStack.class)
public abstract class rinlib$ItemDamageMixin {
    @Inject(
            method =
                    "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            cancellable = true)
    private void rinlib$preserveBeforeDestruction(
            int amount,
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.world.entity.LivingEntity player,
            java.util.function.Consumer<net.minecraft.world.item.Item> onBreak,
            CallbackInfo ci) {
        for (var listener : ItemEvents.BEFORE_BREAK.listeners()) {
            if (listener.onBreak((ItemStack) (Object) this, level, player)) { ci.cancel(); return; }
        }
    }
}
