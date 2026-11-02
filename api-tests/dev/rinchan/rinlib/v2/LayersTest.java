package dev.rinchan.rinlib.v2;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LayersTest {
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }
    @Test void slotsWithTheSameRootKeyShareTheirIdentity() {
        assertSame(PlayerData.slot("test:pending"), PlayerData.slot("test:pending"));
        assertNotSame(PlayerData.slot("test:pending"), PlayerData.slot("test:other"));
    }
    @Test void craftingViewsReceiveThePreviousListenersResult() {
        var first = new ItemStack(net.minecraft.world.item.Items.STONE);
        var second = new ItemStack(net.minecraft.world.item.Items.DIRT);
        RepairEvents.CRAFTING_VIEW.register((s, level) -> { assertSame(first, s); assertNull(level); return second; });
        RepairEvents.CRAFTING_VIEW.register((s, level) -> { assertSame(second, s); return ItemStack.EMPTY; });
        try { assertSame(ItemStack.EMPTY, RepairEvents.craftingView(first, null)); }
        finally { RepairEvents.CRAFTING_VIEW.listeners().clear(); }
    }
}
