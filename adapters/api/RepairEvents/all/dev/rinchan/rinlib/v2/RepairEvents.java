package dev.rinchan.rinlib.v2;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
public final class RepairEvents {
    public static final Event<RepairView> CRAFTING_VIEW = new Event<>();
    public static final Event<AnvilHook> ANVIL = new Event<>();
    private RepairEvents() {}
    @FunctionalInterface public interface RepairView { ItemStack view(ItemStack stack, @Nullable Level level); }
    public interface AnvilHook {
        default boolean blockResult(ItemStack input, Level level) { return false; }
        default ItemStack viewInput(ItemStack input, Level level) { return input; }
        default ItemStack finishResult(ItemStack left, ItemStack right, ItemStack result, RegistryAccess access) { return result; }
    }
    public static ItemStack craftingView(ItemStack stack, @Nullable Level level) {
        for (var listener : CRAFTING_VIEW.listeners()) stack = listener.view(stack, level);
        return stack;
    }
}
