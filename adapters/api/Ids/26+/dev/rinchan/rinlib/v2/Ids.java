package dev.rinchan.rinlib.v2;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
public final class Ids {
    private Ids() {}
    public static TagKey<Item> itemTag(String ns, String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(ns, path));
    }
    public static boolean isValid(String id) { return Identifier.tryParse(id) != null; }
}
