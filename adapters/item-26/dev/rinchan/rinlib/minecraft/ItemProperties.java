package dev.rinchan.rinlib.minecraft;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/** @deprecated 改用 dev.rinchan.rinlib.api.ItemProperties */
@Deprecated(forRemoval = true)
public final class ItemProperties {
    private ItemProperties() {}

    public static Item.Properties of(Identifier id) {
        return dev.rinchan.rinlib.api.ItemProperties.of(id.toString());
    }
}
