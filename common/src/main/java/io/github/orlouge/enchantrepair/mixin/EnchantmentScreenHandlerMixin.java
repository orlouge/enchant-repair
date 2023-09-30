package io.github.orlouge.enchantrepair.mixin;

import io.github.orlouge.enchantrepair.Config;
import io.github.orlouge.enchantrepair.ModifiedEnchantingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Pair;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.*;

@Mixin(EnchantmentScreenHandler.class)
public abstract class EnchantmentScreenHandlerMixin extends ScreenHandler {
    @Shadow @Final private Random random;
    @Shadow @Final private ScreenHandlerContext context;
    @Shadow @Final public int[] enchantmentPower;
    @Shadow @Final private Property seed;

    @Shadow protected abstract List<EnchantmentLevelEntry> generateEnchantments(ItemStack stack, int slot, int level);

    @Shadow @Final public int[] enchantmentId;
    @Shadow @Final public int[] enchantmentLevel;
    private int playerLevel = 30;
    private Collection<ModifiedEnchantingHelper.StoredBook> books = null;
    private boolean applyEnchantments = true;
    private Collection<ModifiedEnchantingHelper.StoredBook> consumedBooks = null;

    protected EnchantmentScreenHandlerMixin(@Nullable ScreenHandlerType<?> type, int syncId) {
        super(type, syncId);
    }

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At(value = "TAIL"))
    public void storePlayerLevelOnInit(int syncId, PlayerInventory playerInventory, ScreenHandlerContext context, CallbackInfo ci) {
        this.playerLevel = playerInventory.player.experienceLevel;
    }

    @Inject(method = "onContentChanged", at = @At("HEAD"))
    public void onContentChangedUpdateBooks(Inventory inventory, CallbackInfo ci) {
        this.applyEnchantments = false;
        if (this.consumedBooks != null) {
            this.context.run((world, tablePos) -> {
                consumeBooks(world, this.consumedBooks);
                this.consumedBooks = null;
            });
        }
        if (this.books == null) this.updateBooks();
    }
    
    @Inject(method = "onContentChanged", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/screen/ScreenHandlerContext;run(Ljava/util/function/BiConsumer;)V"))
    public void onContentChangedUpdateBookOnly(Inventory inventory, CallbackInfo ci, ItemStack stack) {
        if (stack.getItem().getEnchantability() <= 0) {
            this.context.run((world, pos) -> {
                this.random.setSeed(this.seed.get());

                for (int slot = 0; slot <= 2; slot++) {
                    this.enchantmentPower[slot] = 1;
                    List<EnchantmentLevelEntry> enchantments = this.generateEnchantments(stack, slot, 0);
                    if (enchantments != null && !enchantments.isEmpty()) {
                        EnchantmentLevelEntry entry = enchantments.get(this.random.nextInt(enchantments.size()));
                        this.enchantmentId[slot] = Registries.ENCHANTMENT.getRawId(entry.enchantment);
                        this.enchantmentLevel[slot] = entry.level;
                    } else {
                        this.enchantmentPower[slot] = 0;
                        this.enchantmentId[slot] = -1;
                        this.enchantmentLevel[slot] = -1;
                    }
                }

                this.sendContentUpdates();
            });
            ci.cancel();
        }
    }

    private static void consumeBooks(World world, Collection<ModifiedEnchantingHelper.StoredBook> consumedBooks) {
        for (ModifiedEnchantingHelper.StoredBook book : consumedBooks) {
            if (world.getBlockEntity(book.bookshelfPos()) instanceof ChiseledBookshelfBlockEntity entity) {
                ItemStack stack = entity.getStack(book.slot());
                if (stack.isEmpty() || !stack.isOf(Items.ENCHANTED_BOOK)) {
                    continue;
                }
                if (stack.getCount() > 1) {
                    entity.setStack(book.slot(), stack);
                    stack.decrement(1);
                } else {
                    entity.removeStack(book.slot());
                }
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, book.bookshelfPos(), new GameEvent.Emitter(null, null));
                world.updateComparators(book.bookshelfPos(), Blocks.CHISELED_BOOKSHELF);
                world.playSound(null, book.bookshelfPos(), SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP_ENCHANTED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    @Inject(method = "onButtonClick", at = @At("HEAD"))
    public void onButtonClickUpdateBooks(PlayerEntity player, int id, CallbackInfoReturnable<Boolean> cir) {
        this.updateBooks();
        this.applyEnchantments = true;
    }

    @ModifyArg(method = "generateEnchantments", at = @At(value = "INVOKE", target = "Lnet/minecraft/enchantment/EnchantmentHelper;generateEnchantments(Lnet/minecraft/util/math/random/Random;Lnet/minecraft/item/ItemStack;IZ)Ljava/util/List;"), index = 2)
    public int applyEnchantmentLevelPenalty(int level) {
        return level - ModifiedEnchantingHelper.randomEnchantmentLevelPenalty(level, this.playerLevel, this.random);
    }

    @Inject(method = "generateEnchantments", at = @At(value = "RETURN"),  cancellable = true)
    public void addBooksToRandomEnchantments(ItemStack stack, int slot, int level, CallbackInfoReturnable<List<EnchantmentLevelEntry>> cir) {
        List<EnchantmentLevelEntry> randomEnchantments = level <= 0 ? Collections.emptyList() : cir.getReturnValue();
        List<EnchantmentLevelEntry> bookEnchantments = addBookEnchantments(stack, randomEnchantments);
        if (bookEnchantments != null) {
            cir.setReturnValue(bookEnchantments);
        }
    }

    private List<EnchantmentLevelEntry> addBookEnchantments(ItemStack stack, List<EnchantmentLevelEntry> randomEnchantments) {
        if (this.books == null || this.books.size() == 0) return null;
        Pair<List<EnchantmentLevelEntry>, List<ModifiedEnchantingHelper.StoredBook>> generated = ModifiedEnchantingHelper.generateEnchantmentsFromBooks(stack, this.books, this.playerLevel, this.random);
        List<EnchantmentLevelEntry> bookEnchantments = new ArrayList<>(generated.getLeft());
        for (EnchantmentLevelEntry enchantment : randomEnchantments) {
            if (bookEnchantments.stream().anyMatch(e -> !e.enchantment.canCombine(enchantment.enchantment)))
                continue;
            bookEnchantments.add(enchantment);
        }
        if (applyEnchantments) {
            this.consumedBooks = generated.getRight();
            if (stack.getItem().isDamageable() && random.nextDouble() < ModifiedEnchantingHelper.enchantDamageChance(this.playerLevel)) {
                int dmg = stack.getDamage(), max = stack.getItem().getMaxDamage();
                int newdmg = dmg + (max - dmg) * 3 / 10;
                if (newdmg > dmg + 10 && newdmg < max) {
                    stack.setDamage(newdmg);
                }
            }
        }
        return bookEnchantments;
    }

    private void updateBooks() {
        if (Config.BOOK_ENCHANTMENT_ENABLED) {
            this.context.run((world, pos) -> {
                this.books = ModifiedEnchantingHelper.getAvailableEnchantedBooks(world, pos);
            });
        } else {
            this.books = Collections.emptyList();
        }
    }
}
