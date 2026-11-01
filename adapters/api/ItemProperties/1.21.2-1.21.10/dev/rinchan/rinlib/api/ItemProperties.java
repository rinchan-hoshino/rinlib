package dev.rinchan.rinlib.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

/** Item properties carrying the registry ID on Minecraft versions that require it. */
public final class ItemProperties {
    private ItemProperties() {}

    public static Item.Properties of(String id) {
        return new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ResourceLocation.parse(id)));
    }
}
