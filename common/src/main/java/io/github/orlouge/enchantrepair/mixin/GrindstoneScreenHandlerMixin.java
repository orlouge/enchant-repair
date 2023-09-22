package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
public class GrindstoneScreenHandlerMixin {
    @Inject(method = "getExperience(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    public void nullExperience(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (Config.DISABLE_GRINDSTONE_XP) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }
}
