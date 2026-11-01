package dev.rinchan.rinlib.api;

import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MinecraftAdaptersTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void itemIdsAreValidatedEvenBeforeMinecraftRequiresThem() {
        assertNotNull(ItemProperties.of("example:dog_tag"));
        assertThrows(RuntimeException.class, () -> ItemProperties.of("Invalid id!"));
    }

    @Test
    void customItemDataIsCopiedAndEmptyDataClearsThePreviousValue() {
        var data = MinecraftData.of(RegistryAccess.EMPTY);
        var stack = new ItemStack(Items.STONE);
        var tag = new CompoundTag();
        tag.putInt("counter", 3);
        data.customData(stack, tag);
        tag.putInt("counter", 9);
        assertEquals(3, data.customData(stack).getInt("counter"));
        var copy = data.customData(stack);
        copy.putInt("counter", 12);
        assertEquals(3, data.customData(stack).getInt("counter"));
        data.customData(stack, new CompoundTag());
        assertTrue(data.customData(stack).isEmpty());
    }

    @Test
    void literalTextRoundTripsThroughRegistryAwareJson() {
        var data = MinecraftData.of(RegistryAccess.EMPTY);
        var component = Component.literal("quote: \" and newline\n");
        assertEquals(component, data.component(data.json(component)));
        assertThrows(RuntimeException.class, () -> data.component("{invalid"));
    }

    @Test
    void packMetadataEscapesDescriptionsAndDeclaresTheCurrentFormat() {
        var pack = JsonParser.parseString(PackFormats.serverDataMetadata("quote: \"\n"))
                .getAsJsonObject().getAsJsonObject("pack");
        assertEquals("quote: \"\n", pack.get("description").getAsString());
        assertEquals(48, PackFormats.serverDataMajor());
        assertEquals(0, PackFormats.serverDataMinor());
        assertEquals(48, pack.get("pack_format").getAsInt());
    }
}
