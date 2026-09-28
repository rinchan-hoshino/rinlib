package dev.rinchan.rinlib.minecraft;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

/** Item properties carrying the registry ID on Minecraft versions that require it. */
public final class ItemProperties {
    private ItemProperties() {}

    public static Item.Properties of(Identifier id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
    }
}
