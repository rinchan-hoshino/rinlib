package dev.rinchan.rinlibprobe;
import dev.rinchan.rinlib.item.DamageState;
import dev.rinchan.rinlib.minecraft.KeepInventoryProjection;
import dev.rinchan.rinlib.payment.DenominatedPayment;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;
import static dev.rinchan.rinlibprobe.RuntimeProbe.require;
public final class ProfileChecks {
    public static void run(MinecraftServer server) {
        var stack = new ItemStack(Items.IRON_PICKAXE);
        require(!DamageState.isBroken(stack), "new tool is intact");
        stack.setDamageValue(stack.getMaxDamage());
        require(DamageState.isBroken(stack), "exhausted tool is broken");
        require(!DamageState.isBroken(ItemStack.EMPTY), "empty stack is intact");
        require(DamageState.clampDamage(stack, -1) == 0 && DamageState.clampDamage(stack, Integer.MAX_VALUE) == stack.getMaxDamage(), "damage is bounded");
        require(DamageState.clampDamage(new ItemStack(Items.STONE), 10) == 10, "non-damageable values remain unchanged");
        var plan = DenominatedPayment.plan(10, List.of(new DenominatedPayment.Stack(0, 2, 9))).orElseThrow();
        require(plan.changeUnits() == 8 && plan.removals().get(0).itemCount() == 2, "payment conserves value");
        require(!KeepInventoryProjection.isActive(), "projection initially inactive");
        try (var scope = KeepInventoryProjection.enter()) {
            require(KeepInventoryProjection.isActive(), "projection active in scope");
        }
        require(!KeepInventoryProjection.isActive(), "projection clears on close");
        require(!server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY), "real gamerule initially false");
        try (var outer = KeepInventoryProjection.enter()) {
            require(server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY), "mixin projects real gamerule");
            try (var inner = KeepInventoryProjection.enter()) { require(server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY), "nested scope projects gamerule"); }
            require(server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY), "outer scope survives inner close");
        }
        require(!server.getGameRules().getBoolean(net.minecraft.world.level.GameRules.RULE_KEEPINVENTORY), "real gamerule restored after scope");
        var player = ProbePlayers.create(server, "RinLibProbe", 100);
        require(!dev.rinchan.rinlib.minecraft.MobEffectState.add(player, "INVALID ID", 40, 0), "invalid effect rejected");
        require(!dev.rinchan.rinlib.minecraft.MobEffectState.add(player, "minecraft:missing_effect", 40, 0), "unknown effect rejected");
        require(dev.rinchan.rinlib.minecraft.MobEffectState.add(player, "minecraft:speed", 40, 1), "real player receives effect");
        require(player.getActiveEffects().stream().anyMatch(effect -> effect.getDuration() == 40 && effect.getAmplifier() == 1), "effect duration and amplifier retained");
        ApiChecks.run(server, player);
    }
}
