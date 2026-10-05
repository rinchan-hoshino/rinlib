package dev.rinchan.rinlib.mixin.structure;

import dev.rinchan.rinlib.minecraft.StructureLifecycle;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerList.class)
abstract class OperatorLoginMixin {
    @Inject(method = "placeNewPlayer", at = @At("RETURN"))
    private void rinlib$operatorNotice(Connection connection, ServerPlayer player, net.minecraft.server.network.CommonListenerCookie cookie, CallbackInfo callback) {
        PlayerList players = (PlayerList)(Object)this;
        StructureLifecycle.onOperatorLogin(players.isOp(player.getGameProfile()), player::sendSystemMessage);
    }
}
