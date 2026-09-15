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

    }
}
