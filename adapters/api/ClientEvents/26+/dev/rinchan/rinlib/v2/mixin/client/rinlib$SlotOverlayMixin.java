package dev.rinchan.rinlib.v2.mixin.client;


import dev.rinchan.rinlib.v2.ClientEvents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphicsExtractor.class)
public abstract class rinlib$SlotOverlayMixin {
    @Inject(
            method =
                    "itemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("TAIL"))
    private void rinlib$renderBrokenOverlay(
            Font font, ItemStack stack, int x, int y, @Nullable String countText, CallbackInfo ci) {
        for (var listener : ClientEvents.SLOT_OVERLAY.listeners()) {
            int argb = listener.applyAsInt(stack);
            if (argb != 0) { ((GuiGraphicsExtractor) (Object) this).fill(x, y, x + 16, y + 16, argb); return; }
        }
    }
}
