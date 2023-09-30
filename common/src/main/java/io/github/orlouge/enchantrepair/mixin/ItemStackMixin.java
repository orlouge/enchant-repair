package io.github.orlouge.enchantrepair.mixin;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Redirect(method = "isEnchantable", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;hasEnchantments()Z"))
    public boolean cursedItemsAreEnchantable(ItemStack stack) {
        return EnchantmentHelper.get(stack).keySet().stream().anyMatch(ench -> !ench.isCursed() || ench == Enchantments.VANISHING_CURSE);
    }
}
