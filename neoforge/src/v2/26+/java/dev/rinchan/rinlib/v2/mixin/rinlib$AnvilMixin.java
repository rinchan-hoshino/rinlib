package dev.rinchan.rinlib.v2.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;

import dev.rinchan.rinlib.v2.RepairEvents;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Compute on snapshots; vanilla keeps ownership of inputs, XP and the output transaction. */
@Mixin(AnvilMenu.class)
public abstract class rinlib$AnvilMixin extends ItemCombinerMenu {
    protected rinlib$AnvilMixin(
            MenuType<?> type,
            int id,
            Inventory inventory,
            ContainerLevelAccess access,
            ItemCombinerMenuSlotDefinition definition) {
        super(type, id, inventory, access, definition);
    }

    @com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod(method = "createResultInternal")
    private void rinlib$repairScope(Operation<Void> original) {
        for (int i = 0; i < inputSlots.getContainerSize(); i++) {
            var input = inputSlots.getItem(i);
            for (var listener : RepairEvents.ANVIL.listeners()) {
                if (listener.blockResult(input, player.level())) {
                    resultSlots.setItem(0, ItemStack.EMPTY);
                    return;
                }
            }
        }
        original.call();
    }

    @WrapOperation(
            method = "createResultInternal",
            at =
                    @At(
                            value = "INVOKE",
                            target =
                                    "Lnet/minecraft/world/Container;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack rinlib$snapshot(Container container, int slot, Operation<ItemStack> original) {
        var stack = original.call(container, slot);
        for (var listener : RepairEvents.ANVIL.listeners()) stack = listener.viewInput(stack, player.level());
        return stack;
    }

    @Inject(method = "createResultInternal", at = @At("TAIL"))
    private void rinlib$keepBrokenOutputSealed(CallbackInfo ci) {
        var result = resultSlots.getItem(0);
        var finished = result;
        for (var listener : RepairEvents.ANVIL.listeners()) {
            finished = listener.finishResult(inputSlots.getItem(0), inputSlots.getItem(1), finished, player.level().registryAccess());
        }
        if (finished != result) resultSlots.setItem(0, finished);
    }
}
