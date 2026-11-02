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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public abstract class rinlib$ItemDamageMixin {
    @Unique private net.minecraft.world.entity.LivingEntity rinlib$owner;
    @WrapOperation(
        method = "hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"))
    private void rinlib$withOwner(ItemStack stack, int damage, net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player, java.util.function.Consumer<net.minecraft.world.item.Item> onBreak,
            Operation<Void> original,
            @com.llamalad7.mixinextras.sugar.Local(argsOnly = true) net.minecraft.world.entity.LivingEntity owner) {
        var previous = rinlib$owner;
        rinlib$owner = owner;
        try { original.call(stack, damage, level, player, onBreak); }
        finally { rinlib$owner = previous; }
    }

    @Unique private ServerLevel rinlib$breakLevel;

    // 26.x delegates to a level-less applyDamage; retain the level only for that native call.
    @WrapOperation(
            method =
                    "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/item/ItemStack;applyDamage(ILnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V"))
    private void rinlib$withBreakLevel(
            ItemStack stack,
            int damage,
            net.minecraft.server.level.ServerPlayer owner,
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
                    "applyDamage(ILnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            cancellable = true)
    private void rinlib$preserve(
            int damage,
            net.minecraft.server.level.ServerPlayer owner,
            Consumer<Item> onBreak,
            CallbackInfo ci) {
        if (rinlib$breakLevel == null) return;
        var stack = (ItemStack) (Object) this;
        for (var listener : ItemEvents.BEFORE_BREAK.listeners()) {
            if (listener.onBreak(stack, rinlib$breakLevel, owner != null ? owner : rinlib$owner)) { ci.cancel(); return; }
        }
    }
}
