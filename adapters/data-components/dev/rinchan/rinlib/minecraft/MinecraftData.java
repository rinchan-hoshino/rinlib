package dev.rinchan.rinlib.minecraft;

import com.google.gson.JsonElement;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/** @deprecated 改用 dev.rinchan.rinlib.api.MinecraftData */
@Deprecated(forRemoval = true)
public final class MinecraftData {
    private final dev.rinchan.rinlib.api.MinecraftData delegate;

    private MinecraftData(HolderLookup.Provider registries) {
        delegate = dev.rinchan.rinlib.api.MinecraftData.of(registries);
    }

    public static MinecraftData of(HolderLookup.Provider registries) {
        return new MinecraftData(registries);
    }

    public Component component(JsonElement json) { return delegate.component(json); }
    public Component component(String json) { return delegate.component(json); }
    public String json(Component component) { return delegate.json(component); }
    public CompoundTag saveItem(ItemStack stack) { return delegate.saveItem(stack); }
    public Optional<ItemStack> loadItem(CompoundTag tag) { return delegate.loadItem(tag); }
    public CompoundTag customData(ItemStack stack) { return delegate.customData(stack); }
    public void customData(ItemStack stack, CompoundTag data) { delegate.customData(stack, data); }
    public void decorate(ItemStack stack, Component name, List<Component> lore) { delegate.decorate(stack, name, lore); }
}
