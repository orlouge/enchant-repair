package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.*;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {
    @Shadow private int repairItemUsage;
    @Shadow @Final private Property levelCost;
    private ItemStack savedSecondItem = ItemStack.EMPTY;
    private boolean isRename = false;
    private boolean modifiersApplied = false;

    public AnvilScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;copy()Lnet/minecraft/item/ItemStack;"), cancellable = true)
    public void disableMerging(CallbackInfo ci) {
        ItemStack input = this.input.getStack(0);
        ItemStack repair = this.input.getStack(1);
        boolean isRepairingOrMerging = !repair.isEmpty() && (input.isDamageable() || input.isOf(Items.ENCHANTED_BOOK));
        boolean nonDisposable = Config.REPAIR_VANISHING || EnchantmentHelper.getLevel(Enchantments.VANISHING_CURSE, input) <= 0;
        boolean repairable = input.isDamageable() && this.canRepairExtended(input.getItem(), input, repair) && nonDisposable;
        boolean mergeable = (this.player.isCreative() && Config.ALLOW_CREATIVE_ANVIL_MERGE) || Config.ALLOW_SURVIVAL_ANVIL_MERGE;
        if (isRepairingOrMerging && !repairable && !mergeable) {
            this.output.setStack(0, ItemStack.EMPTY);
            this.levelCost.set(0);
            ci.cancel();
        }
    }

    @Inject(method = "updateResult", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/screen/AnvilScreenHandler;sendContentUpdates()V"))
    public void modifyResult(CallbackInfo ci) {
        if (Config.DISABLE_ANVIL_XP_COST) {
            this.levelCost.set(0);
        }
    }

    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I", ordinal = 0))
    public int calculateRepairAmount(int dmg, int maxDmg4) {
        return Config.REPAIR_CHEAP ? dmg : Math.min(dmg, maxDmg4);
    }

    @Redirect(method = "updateResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;canRepair(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Z"))
    public boolean canRepairExtended(Item item, ItemStack stack, ItemStack ingredient) {
        if (item.canRepair(stack, ingredient)) return true;
        Set<Item> extraRepairItems = Config.REPAIR_EXTRA_ITEMS.get().get(item);
        return extraRepairItems != null && extraRepairItems.contains(ingredient.getItem());
    }

    @Inject(method = "getNextCost", at = @At("HEAD"), cancellable = true)
    private static void removeRepairCost(int cost, CallbackInfoReturnable<Integer> cir) {
        if (Config.DISABLE_ANVIL_XP_COST) {
            cir.setReturnValue(0);
            cir.cancel();
        }
    }

    @Inject(method = "canTakeOutput", at = @At("RETURN"), cancellable = true)
    private void alwaysTakeOutput(PlayerEntity player, boolean present, CallbackInfoReturnable<Boolean> cir) {
        if (Config.DISABLE_ANVIL_XP_COST) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    private void applyModifiers(PlayerEntity player, ItemStack result) {
        if (!(player instanceof ServerPlayerEntity)) return;
        ItemStack item2 = this.input.getStack(1);
        this.isRename = item2.isEmpty();
        if (this.repairItemUsage <= 0) return;
        ItemStack item1 = this.input.getStack(0);
        if (!item1.isEmpty() && (item1.isDamageable() || item1.isEnchantable()) && !item2.isEmpty() && this.canRepairExtended(item1.getItem(), item1, item2)) {
            boolean hasMending = EnchantmentHelper.getLevel(Enchantments.MENDING, item1) > 0;
            float levelScaled = (float) Math.max(1, player.experienceLevel * Math.sqrt(player.experienceLevel));
            float consumeChance = 1.65f * Config.REPAIR_CONSUME_CHANCE / levelScaled;
            float failChance = Config.REPAIR_CONSUME_CHANCE > 0 ? Config.REPAIR_FAIL_CHANCE / Config.REPAIR_CONSUME_CHANCE : 0f;
            if (hasMending) consumeChance *= 0.7;
            int nonConsumed = (int) (Math.log(player.getRandom().nextFloat()) / Math.log(1 - consumeChance));
            if (this.repairItemUsage <= nonConsumed) {
                this.savedSecondItem = item2.copy();
            } else {
                this.repairItemUsage -= nonConsumed;
                if (failChance > 0) {
                    if (hasMending) failChance *= 0.2;
                    failChance = Math.min(0.2f, failChance);
                    if (player.getRandom().nextFloat() < failChance) {
                        result.setDamage(result.getMaxDamage() - 1);
                        this.context.run((world, pos) -> world.playSound(null, pos,
                                SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.BLOCKS, 1.5F, 1.2F));
                        return;
                    }
                }
            }

            float disenchantChance = 1.65f * Config.REPAIR_DISENCHANT_CHANCE / levelScaled;
            if (hasMending) disenchantChance *= 0.2;
            if (failChance > 0) disenchantChance /= failChance;
            disenchantChance = Math.min(0.3f, disenchantChance);
            Map<Enchantment, Integer> enchantments = new LinkedHashMap<>(EnchantmentHelper.get(result));
            if (player.getRandom().nextFloat() < disenchantChance) {
                ArrayList<Enchantment> choiceList = new ArrayList<>();
                enchantments.forEach((ench, level) -> { if (!ench.isCursed() && !ench.isTreasure()) for (int i = 0; i < level; i++) choiceList.add(ench);});
                if (choiceList.size() == 0) return;
                Enchantment lost = choiceList.get(player.getRandom().nextInt(choiceList.size()));
                int level = enchantments.get(lost);
                if (level <= 1) enchantments.remove(lost);
                else enchantments.put(lost, level - 1);
                EnchantmentHelper.set(enchantments, result);
                this.context.run((world, pos) -> world.playSound(null, pos,
                        SoundEvents.BLOCK_GRINDSTONE_USE, SoundCategory.BLOCKS, 1.5F, 1.5F));
            }
        }
    }


    @Override
    public ItemStack quickMove(PlayerEntity player, int slotidx) {
        if (!this.modifiersApplied && slotidx == this.getResultSlotIndex()) {
            Slot slot = this.getSlot(slotidx);
            if (slot != null) {
                ItemStack stack = slot.getStack();
                applyModifiers(player, stack);
                slot.markDirty();
                this.modifiersApplied = true;
            }
        }
        return super.quickMove(player, slotidx);
    }

    @Inject(method = "onTakeOutput", at = @At("HEAD"))
    public void saveRepairItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (!this.modifiersApplied) applyModifiers(player, stack);
        this.modifiersApplied = false;
    }

    @Inject(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/Property;set(I)V"))
    public void restoreSavedRepairItem(PlayerEntity player, ItemStack stack, CallbackInfo ci) {
        if (this.savedSecondItem != null && !this.savedSecondItem.isEmpty()) {
            this.input.setStack(1, this.savedSecondItem);
            this.savedSecondItem = ItemStack.EMPTY;
        }
    }

    @Redirect(method = "onTakeOutput", at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"))
    public void dontDamageAnvilOnRename(ScreenHandlerContext context, BiConsumer<World, BlockPos> function) {
        if (this.isRename && Config.DISABLE_ANVIL_DAMAGE_ON_RENAME) {
            this.isRename = false;
            context.run((world, pos) -> world.syncWorldEvent(1030, pos, 0));
        } else {
            context.run(function);
        }
    }
}
