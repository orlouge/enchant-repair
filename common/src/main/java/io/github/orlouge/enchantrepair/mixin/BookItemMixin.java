package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import net.minecraft.item.BookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BookItem.class)
public class BookItemMixin extends Item {
    public BookItemMixin(Settings settings) {
        super(settings);
    }

    @Inject(method = "isEnchantable", at = @At("RETURN"), cancellable = true)
    public void isEnchantable(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (Config.DISABLE_ENCHANTING_BOOKS) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
