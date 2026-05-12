package dev.rinchan.rinlib.item;

import net.minecraft.item.ItemStack;

/** Shared item-durability state helpers for RinChan mods. */
public final class DamageState {
    public static final int BROKEN_SLOT_OVERLAY_ARGB = 0x66FF0000;
    public static final float BROKEN_SURFACE_RED = 1.0F;
    public static final float BROKEN_SURFACE_GREEN = 0.18F;
    public static final float BROKEN_SURFACE_BLUE = 0.18F;

    private DamageState() {}

    public static boolean isBroken(ItemStack stack) {
        return !stack.isEmpty() && stack.isItemStackDamageable() && stack.getMaxDamage() > 0 && stack.getItemDamage() >= stack.getMaxDamage();
    }

    public static int clampDamage(ItemStack stack, int damage) {
        if (!stack.isItemStackDamageable()) return damage;
        return Math.max(0, Math.min(damage, stack.getMaxDamage()));
    }
}
