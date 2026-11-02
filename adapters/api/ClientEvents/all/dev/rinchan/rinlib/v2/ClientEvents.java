package dev.rinchan.rinlib.v2;

import java.util.List;
import java.util.function.ToIntFunction;
import java.util.function.UnaryOperator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;
public final class ClientEvents {
    public static final Event<TooltipOverride> TOOLTIP = new Event<>();
    public static final Event<UnaryOperator<ItemStack>> RENDER_STACK = new Event<>();
    public static final Event<ToIntFunction<ItemStack>> SLOT_OVERLAY = new Event<>();
    private ClientEvents() {}
    @FunctionalInterface public interface TooltipOverride {
        @Nullable List<Component> override(ItemStack s, Item.TooltipContext ctx, @Nullable Player p, TooltipFlag f);
    }
}
