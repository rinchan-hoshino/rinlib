package dev.rinchan.rinlib.v2;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
public final class Nbt {
    private Nbt() {}
    public static int getInt(CompoundTag t, String key) { return t.getIntOr(key, 0); }
    public static String getString(ListTag l, int i) { return l.getString(i).orElseThrow(); }
}
