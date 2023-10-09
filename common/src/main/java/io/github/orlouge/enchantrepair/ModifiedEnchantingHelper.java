package io.github.orlouge.enchantrepair;

import net.minecraft.block.BarrelBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.*;

public class ModifiedEnchantingHelper {
    public static final double BOOK_CHANCE_MEAN_INTERCEPT = 0;
    public static final double BOOK_CHANCE_MEAN_SLOPE = 0.4;
    public static final double BOOK_CHANCE_STD_INTERCEPT = 0.75;
    public static final double BOOK_CHANCE_STD_SLOPE = 0.175;

    public static int randomEnchantmentLevelPenalty(int maxLevel, int playerLevel, Random random) {
        if (!Config.RANDOM_ENCHANTMENT_PENALTY) return 0;
        float mean = Config.RANDOM_ENCHANTMENT_PENALTY_MEAN_INTERCEPT - Config.RANDOM_ENCHANTMENT_PENALTY_MEAN_SLOPE * playerLevel;
        return (int) Math.min(maxLevel / 2, Math.max(0, Math.round(random.nextGaussian() * Config.RANDOM_ENCHANTMENT_PENALTY_STDDEV + mean)));
    }

    public static int maximumBookCostAtLevel(int playerLevel, Random random) {
        double level = playerLevel * Config.BOOK_ENCHANTMENT_LEVEL_FACTOR;
        double mean = BOOK_CHANCE_MEAN_INTERCEPT + BOOK_CHANCE_MEAN_SLOPE * level;
        double std = BOOK_CHANCE_STD_INTERCEPT + BOOK_CHANCE_STD_SLOPE * level;
        return (int) Math.ceil(random.nextGaussian() * std + mean);
    }

    public static double enchantDamageChance(int playerLevel) {
        return Math.min(Config.ENCHANT_DAMAGE_CHANCE * 0.1, (double) 9 * Config.ENCHANT_DAMAGE_CHANCE / Math.max(1, playerLevel * playerLevel));
    }

    public static Collection<StoredBook> getAvailableEnchantedBooks(World world, BlockPos tablePos) {
        List<StoredBook> books = new LinkedList<>();
        for (BlockPos offset : EnchantingTableBlock.BOOKSHELF_OFFSETS) {
            BlockPos pos = tablePos.add(offset);
            if (world.getBlockState(pos).getBlock() instanceof BarrelBlock && world.getBlockEntity(pos) instanceof BarrelBlockEntity entity) {
                for (int slot = 0; slot < entity.size(); slot++) {
                    ItemStack itemStack = entity.getStack(slot);
                    if (itemStack != null && !itemStack.isEmpty() && itemStack.isOf(Items.ENCHANTED_BOOK)) {
                        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(itemStack);
                        if (enchantments.size() > 0) {
                            books.add(new StoredBook(pos, slot, enchantments));
                        }
                    }
                }
            }
        }
        return books;
    }

    public static Pair<List<EnchantmentLevelEntry>, List<StoredBook>> generateEnchantmentsFromBooks(ItemStack item, Collection<StoredBook> books, int playerLevel, Random random) {
        List<StoredBook> remainingBooks = new ArrayList<>(books);
        List<StoredBook> consumedBooks = new LinkedList<>();
        Set<Enchantment> attemptedEnchantments = new HashSet<>();
        Map<Enchantment, Integer> preexistingEnchantments = EnchantmentHelper.get(item);
        List<EnchantmentLevelEntry> selectedEnchantments = new LinkedList<>();
        int accumulatedCost = 0;
        int maxCost = maximumBookCostAtLevel(playerLevel, random);
        while (remainingBooks.size() > 0) {
            StoredBook book = remainingBooks.remove(random.nextInt(remainingBooks.size()));
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            boolean invalid = false;
            for (Map.Entry<Enchantment, Integer> enchantment : book.enchantments.entrySet()) {
                if (!enchantment.getKey().isAcceptableItem(item) || preexistingEnchantments.containsKey(enchantment.getKey())) continue;
                if (attemptedEnchantments.contains(enchantment.getKey()) ||selectedEnchantments.stream().anyMatch(e2 -> !enchantment.getKey().canCombine(e2.enchantment))) {
                    invalid = true;
                    break;
                }
                enchantments.put(enchantment.getKey(), enchantment.getValue());
            }
            if (invalid) continue;
            int cost = 0;
            for (Map.Entry<Enchantment, Integer> enchantment : enchantments.entrySet()) {
                int enchCost = 0;
                enchCost += Math.round(Math.max(1, (float) 5 * enchantment.getValue() / enchantment.getKey().getMaxLevel()));
                if (enchantment.getKey().isTreasure() && !enchantment.getKey().isCursed()) enchCost += 1;
                cost = Math.max(cost, enchCost);
            }
            if (accumulatedCost > maxCost) break;
            accumulatedCost += cost;
            attemptedEnchantments.addAll(enchantments.keySet());
            enchantments.entrySet().stream().map(e -> new EnchantmentLevelEntry(e.getKey(), e.getValue())).forEach(selectedEnchantments::add);
            if (book.consume()) {
                consumedBooks.add(book);
            }
        }
        return new Pair<>(selectedEnchantments, consumedBooks);
    }

    public record StoredBook(BlockPos bookshelfPos, int slot, Map<Enchantment, Integer> enchantments) {
        public boolean consume() {
            return this.enchantments.keySet().stream().anyMatch(e -> (Config.BOOK_ENCHANTMENT_CONSUME_VANISHING && e.equals(Enchantments.VANISHING_CURSE)) || (Config.BOOK_ENCHANTMENT_CONSUME_TREASURE && e.isTreasure()));
        }
    }
}
