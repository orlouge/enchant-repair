package io.github.orlouge.enchantrepair.fabric.mixin;

import io.github.orlouge.enchantrepair.ModifiedGrindstoneHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerMixin {
    @Redirect(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 0))
    public void onTakeItemModifyTop(Inventory input, int slot, ItemStack empty, PlayerEntity player, ItemStack result) {
        Optional<ItemStack> newTopSlot = ModifiedGrindstoneHelper.modifyTopSlot(input.getStack(0), input.getStack(1));
        if (newTopSlot.isPresent()) {
            input.setStack(0, newTopSlot.get());
            return;
        }
        input.setStack(slot, empty);
    }

    @Redirect(method = "onTakeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;setStack(ILnet/minecraft/item/ItemStack;)V", ordinal = 1))
    public void onTakeItemModifyBottom(Inventory input, int slot, ItemStack empty, PlayerEntity player, ItemStack result) {
        Optional<ItemStack> newBottom = ModifiedGrindstoneHelper.modifyBottomSlot(input.getStack(slot));
        if (newBottom.isPresent()) {
            input.setStack(slot, newBottom.get());
            return;
        }
        input.setStack(slot, empty);
    }
}
