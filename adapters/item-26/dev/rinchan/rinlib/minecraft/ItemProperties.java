package dev.rinchan.rinlib.minecraft;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/** @deprecated Import {@code dev.rinchan.rinlib.api.ItemProperties} instead. */
@Deprecated
public final class ItemProperties {
    private ItemProperties() {}

    public static Item.Properties of(Identifier id) {
        return dev.rinchan.rinlib.api.ItemProperties.of(id.toString());
    }
}
