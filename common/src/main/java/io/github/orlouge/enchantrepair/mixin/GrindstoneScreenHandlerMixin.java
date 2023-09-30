package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import io.github.orlouge.enchantrepair.ModifiedGrindstoneHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GrindstoneScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(GrindstoneScreenHandler.class)
public abstract class GrindstoneScreenHandlerMixin extends ScreenHandler {
    @Shadow @Final Inventory input;

    @Shadow @Final private Inventory result;

    protected GrindstoneScreenHandlerMixin(ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Redirect(method = "grind", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    public Stream<Map.Entry<Enchantment, Integer>> keepTreasureEnchantments(Stream<Map.Entry<Enchantment, Integer>> stream, Predicate<? super Map.Entry<Enchantment, Integer>> defaultFilter) {
        if (Config.GRINDSTONE_DISENCHANT_KEEP_TREASURE) {
            Map<Enchantment, Integer> result = new LinkedHashMap<>();
            boolean[] hasNonTreasure = new boolean[] {false};
            stream.forEach(entry -> {
                if (defaultFilter.test(entry) || entry.getKey().isTreasure()) result.put(entry.getKey(), entry.getValue());
                else hasNonTreasure[0] = true;
            });
            return hasNonTreasure[0] ? result.entrySet().stream() : result.entrySet().stream().filter(defaultFilter);
        }
        return stream.filter(defaultFilter);
    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I", ordinal = 0), cancellable = true)
    public void updateResultWithBook(CallbackInfo ci) {
        if (!Config.GRINDSTONE_EXTRACT_TREASURE) return;
        ItemStack tool = this.input.getStack(0);
        ItemStack book = this.input.getStack(1);
        if (book.isOf(Items.BOOK)) {
            ItemStack result = ItemStack.EMPTY;
            if (!tool.isEmpty() && !book.isEmpty() && !book.hasNbt() && tool.getCount() == 1 && tool.hasEnchantments()) {
                Map<Enchantment, Integer> transfer = ModifiedGrindstoneHelper.filterEnchantments(tool, false, true, false);
                if (transfer != null && transfer.size() > 0) {
                    result = new ItemStack(Items.ENCHANTED_BOOK);
                    EnchantmentHelper.set(transfer, result);
                }
            }
            this.result.setStack(0, result);
            this.sendContentUpdates();
            ci.cancel();
        }
    }

    @Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$3")
    public static class SecondSlotMixin extends Slot {
        public SecondSlotMixin(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Inject(method = "canInsert(Lnet/minecraft/item/ItemStack;)Z", at = @At("RETURN"), cancellable = true)
        public void canInsertBook(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(cir.getReturnValue() || (Config.GRINDSTONE_EXTRACT_TREASURE && stack.isOf(Items.BOOK)));
        }

        /*
        @Override
        public int getMaxItemCount(ItemStack stack) {
            return stack.isOf(Items.BOOK) ? Math.min(1, super.getMaxItemCount(stack)) : super.getMaxItemCount(stack);
        }
         */
    }

    @Mixin(targets = "net.minecraft.screen.GrindstoneScreenHandler$4")
    public static class ResultSlotMixin {
        @Inject(method = "getExperience(Lnet/minecraft/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
        public void nullExperience(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
            if (Config.GRINDSTONE_DISABLE_XP) {
                cir.setReturnValue(0);
                cir.cancel();
            }
        }
    }
}
