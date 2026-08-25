package dev.rinchan.rinlib.mixin;

import dev.rinchan.rinlib.minecraft.KeepInventoryProjection;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRules.class)
abstract class GameRulesMixin {
    @Inject(method = "getBoolean", at = @At("HEAD"), cancellable = true)
    private void rinlib$projectKeepInventory(
            GameRules.Key<GameRules.BooleanValue> key,
            CallbackInfoReturnable<Boolean> result
    ) {
        if (key == GameRules.RULE_KEEPINVENTORY && KeepInventoryProjection.isActive()) {
            result.setReturnValue(true);
        }
    }
}
