package dev.rinchan.rinlib.v2.mixin;

import dev.rinchan.rinlib.v2.PlayerData;
import dev.rinchan.rinlib.v2.internal.PlayerDataAccess;
import java.util.Map;
import java.util.HashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class rinlib$PlayerDataMixin implements PlayerDataAccess {
    @Unique private Map<String, CompoundTag> rinlib$data = new HashMap<>();
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void rinlib$save(ValueOutput output, CallbackInfo callback) { for (var key : PlayerData.keys()) { var value = rinlib$data.get(key); if (value != null) output.store(key, CompoundTag.CODEC, value.copy()); } }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void rinlib$load(ValueInput input, CallbackInfo callback) { for (var key : PlayerData.keys()) { var value = input.read(key, CompoundTag.CODEC).map(CompoundTag::copy); if (value.isPresent()) rinlib$data.put(key, value.get()); else rinlib$data.remove(key); } }
    @Override public Map<String, CompoundTag> rinlib$data() { return rinlib$data; }
}
