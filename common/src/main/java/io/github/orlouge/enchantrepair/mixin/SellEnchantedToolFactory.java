package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = "net/minecraft/village/TradeOffers$SellEnchantedToolFactory")
public class SellEnchantedToolFactory {
    @ModifyVariable(method = "create", at = @At(value = "STORE", ordinal = 0))
    public ItemStack addCurseOfVanishing(ItemStack stack, Entity entity, Random random) {
        if (Config.CURSE_TRADED_TOOLS) {
            stack.addEnchantment(Enchantments.VANISHING_CURSE, 1);
        }
        return stack;
    }
}
