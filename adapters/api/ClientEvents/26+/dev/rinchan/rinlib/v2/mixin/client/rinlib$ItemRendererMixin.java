package dev.rinchan.rinlib.v2.mixin.client;


import dev.rinchan.rinlib.v2.ClientEvents;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Only renderer arguments receive the original appearance; inventory identity stays inert. */
@Mixin(ItemModelResolver.class)
public abstract class rinlib$ItemRendererMixin {
    @ModifyVariable(
            method = {"updateForTopItem", "appendItemLayers"},
            at = @At("HEAD"),
            argsOnly = true)
    private ItemStack rinlib$displayOriginal(ItemStack stack) {
        for (var listener : ClientEvents.RENDER_STACK.listeners()) stack = listener.apply(stack);
        return stack;
    }
}
