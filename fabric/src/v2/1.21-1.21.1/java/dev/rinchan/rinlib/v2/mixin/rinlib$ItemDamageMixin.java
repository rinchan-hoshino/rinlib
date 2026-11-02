package dev.rinchan.rinlib.v2.mixin;


import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(
            method =
                    "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            cancellable = true)
    private void rinlib$preserveBeforeDestruction(
            int amount,
            net.minecraft.server.level.ServerLevel level,
            net.minecraft.server.level.ServerPlayer player,
            java.util.function.Consumer<net.minecraft.world.item.Item> onBreak,
            CallbackInfo ci) {
        for (var listener : ItemEvents.BEFORE_BREAK.listeners()) {
            if (listener.onBreak((ItemStack) (Object) this, level, player != null ? player : rinlib$owner)) { ci.cancel(); return; }
        }
    }
}
