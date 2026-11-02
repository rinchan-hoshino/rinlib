package dev.rinchan.rinlib.v2.mixin.client;


import dev.rinchan.rinlib.v2.ClientEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class rinlib$TooltipMixin {
    @Inject(method = "getTooltipLines", at = @At("HEAD"), cancellable = true)
    private void rinlib$originalTooltip(
            Item.TooltipContext context,
            Player player,
            TooltipFlag flag,
            CallbackInfoReturnable<List<Component>> cir) {
        var stack = (ItemStack) (Object) this;
        for (var listener : ClientEvents.TOOLTIP.listeners()) {
            var lines = listener.override(stack, context, player, flag);
            if (lines != null) { cir.setReturnValue(lines); return; }
        }
    }
}
