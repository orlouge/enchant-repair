package io.github.orlouge.enchantrepair;

import net.minecraft.block.ChiseledBookshelfBlock;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
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
    public static final double[] BOOK_CHANCE_PARAMETERS = new double[] {
            3.18836164e+02, -1.57588029e+01,  2.31447236e+02,  8.51072518e-02,
            -3.16729568e+02,  1.57731425e+01,  2.16140796e+02,  8.18513863e-02,
            1.73438225e+03, -2.00192503e+01,  4.30404865e+00, -1.06927041e+00,
            2.66221655e+03,  2.67876822e+03
    };

    public static int randomEnchantmentLevelPenalty(int maxLevel, int playerLevel, Random random) {
        if (!Config.RANDOM_ENCHANTMENT_PENALTY) return 0;
        float mean = Config.RANDOM_ENCHANTMENT_PENALTY_MEAN_INTERCEPT - Config.RANDOM_ENCHANTMENT_PENALTY_MEAN_SLOPE * playerLevel;
        return (int) Math.min(maxLevel / 2, Math.max(0, Math.round(random.nextGaussian() * Config.RANDOM_ENCHANTMENT_PENALTY_STDDEV + mean)));
    }

    public static double bookApplyChancePerEnchantmentLevel(int cost, int playerLevel) {
        double invCost = (double) 1 / Math.max(1, cost), prob = 0, level = playerLevel * Config.BOOK_ENCHANTMENT_LEVEL_FACTOR;
        int i;
        for (i = 0; i < BOOK_CHANCE_PARAMETERS.length - 2; i += 4) {
            prob += level_logistic(invCost, level, BOOK_CHANCE_PARAMETERS[i], BOOK_CHANCE_PARAMETERS[i + 1], BOOK_CHANCE_PARAMETERS[i + 2], BOOK_CHANCE_PARAMETERS[i + 3], 1);
        }
        return level_logistic(prob, level, BOOK_CHANCE_PARAMETERS[i], 0, 1, 0, BOOK_CHANCE_PARAMETERS[i + 1]);
    }

    public static double enchantDamageChance(int playerLevel) {
        return Math.min(Config.ENCHANT_DAMAGE_CHANCE * 0.1, (double) 9 * Config.ENCHANT_DAMAGE_CHANCE / Math.max(1, playerLevel * playerLevel));
    }

    public static Collection<StoredBook> getAvailableEnchantedBooks(World world, BlockPos tablePos) {
        List<StoredBook> books = new LinkedList<>();
        for (BlockPos offset : EnchantingTableBlock.POWER_PROVIDER_OFFSETS) {
            BlockPos pos = tablePos.add(offset);
            if (world.getBlockState(pos).getBlock() instanceof ChiseledBookshelfBlock && world.getBlockEntity(pos) instanceof ChiseledBookshelfBlockEntity entity) {
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
        List<EnchantmentLevelEntry> selectedEnchantments = new LinkedList<>();
        int accumulatedCost = 0;
        while (remainingBooks.size() > 0) {
            StoredBook book = remainingBooks.remove(random.nextInt(remainingBooks.size()));
            Map<Enchantment, Integer> enchantments = new HashMap<>();
            boolean invalid = false;
            for (Map.Entry<Enchantment, Integer> enchantment : book.enchantments.entrySet()) {
                if (!enchantment.getKey().isAcceptableItem(item)) continue;
                if (attemptedEnchantments.contains(enchantment.getKey()) || selectedEnchantments.stream().anyMatch(e2 -> !enchantment.getKey().canCombine(e2.enchantment))) {
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
            if (random.nextDouble() > bookApplyChancePerEnchantmentLevel(cost + accumulatedCost, playerLevel)) continue;
            accumulatedCost += cost;
            attemptedEnchantments.addAll(enchantments.keySet());
            enchantments.entrySet().stream().map(e -> new EnchantmentLevelEntry(e.getKey(), e.getValue())).forEach(selectedEnchantments::add);
            if (book.consume()) {
                consumedBooks.add(book);
            }
        }
        return new Pair<>(selectedEnchantments, consumedBooks);
    }

    private static double level_logistic(double x, double l, double L, double l_m, double k, double x0, double e) {
        return (l_m * l + L) / (e + Math.exp(k * (x - x0)));
    }

    public record StoredBook(BlockPos bookshelfPos, int slot, Map<Enchantment, Integer> enchantments) {
        public boolean consume() {
            return this.enchantments.keySet().stream().anyMatch(e -> e.equals(Enchantments.VANISHING_CURSE));
        }
    }
}
