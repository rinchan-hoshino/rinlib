package dev.rinchan.rinlib.mixin.structure;

import dev.rinchan.rinlib.minecraft.StructureLifecycle;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
abstract class ServerStopMixin {
    @Inject(method = "stopServer", at = @At("RETURN"))
    private void rinlib$releaseStructureResources(CallbackInfo callback) {
        StructureLifecycle.closeAll();
    }
}
