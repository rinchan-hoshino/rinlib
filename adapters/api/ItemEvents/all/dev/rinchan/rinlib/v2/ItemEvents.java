package dev.rinchan.rinlib.v2;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
public final class ItemEvents {
    public static final Event<BeforeBreak> BEFORE_BREAK = new Event<>();
    private ItemEvents() {}
    @FunctionalInterface public interface BeforeBreak {
        boolean onBreak(ItemStack stack, ServerLevel level, @Nullable LivingEntity owner);
    }
}
