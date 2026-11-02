package dev.rinchan.rinlib.v2;

import dev.rinchan.rinlib.v2.internal.ReplaceableStack;
import net.minecraft.world.item.ItemStack;
public final class Items {
    private Items() {}
    public static void replaceContents(ItemStack target, ItemStack replacement) {
        ((ReplaceableStack) (Object) target).rinlib$replaceWith(replacement);
    }
}
