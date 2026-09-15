package dev.rinchan.rinlibprobe;
import dev.rinchan.rinlib.item.DamageState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import static dev.rinchan.rinlibprobe.RuntimeProbe.require;
public final class ProfileChecks {
    public static void run(MinecraftServer server) {
        ItemStack stack = new ItemStack(Items.IRON_PICKAXE);
        require(!DamageState.isBroken(stack), "new tool is intact");
        stack.setDamageValue(stack.getMaxDamage());
        require(DamageState.isBroken(stack), "exhausted tool is broken");
        require(!DamageState.isBroken(ItemStack.EMPTY), "empty stack is intact");
        require(DamageState.clampDamage(stack, -1) == 0 && DamageState.clampDamage(stack, Integer.MAX_VALUE) == stack.getMaxDamage(), "damage is bounded");
        require(DamageState.clampDamage(new ItemStack(Items.STONE), 10) == 10, "non-damageable values remain unchanged");

    }
}
