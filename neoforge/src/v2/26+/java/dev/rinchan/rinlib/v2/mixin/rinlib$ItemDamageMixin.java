package dev.rinchan.rinlib.v2.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;


import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.*;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

import dev.rinchan.rinlib.v2.ItemEvents;

@Mixin(ItemStack.class)
public abstract class rinlib$ItemDamageMixin {

    @Unique private ServerLevel rinlib$breakLevel;

    // 26.x delegates to a level-less applyDamage; retain the level only for that native call.
    @WrapOperation(
            method =
                    "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private void rinlib$withBreakLevel(
            ItemStack stack,
            int damage,
            net.minecraft.world.entity.LivingEntity owner,
            Consumer<Item> onBreak,
            Operation<Void> original,
            @Local(argsOnly = true) ServerLevel level) {
        var previous = rinlib$breakLevel;
        rinlib$breakLevel = level;
        try {
            original.call(stack, damage, owner, onBreak);
        } finally {
            rinlib$breakLevel = previous;
        }
    }

    @Inject(
            method =
                    "applyDamage(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            cancellable = true)
    private void rinlib$preserve(
            int damage,
            net.minecraft.world.entity.LivingEntity owner,
            Consumer<Item> onBreak,
            CallbackInfo ci) {
        if (rinlib$breakLevel == null) return;
        var stack = (ItemStack) (Object) this;
        for (var listener : ItemEvents.BEFORE_BREAK.listeners()) {
            if (listener.onBreak(stack, rinlib$breakLevel, owner)) { ci.cancel(); return; }
        }
    }
}
