package io.github.orlouge.enchantrepair;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ModifiedGrindstoneHelper {
    public static Map<Enchantment, Integer> extractEnchantments(ItemStack tool, boolean treasure) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(tool), extracted = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> enchEntry : enchantments.entrySet()) {
            if (enchEntry.getValue() == 0) continue;
            if (enchEntry.getKey().isCursed()) {
                return null;
            }
            if (enchEntry.getKey().isTreasure() == treasure) {
                extracted.put(enchEntry.getKey(), enchEntry.getValue());
            }
        }
        return extracted;
    }

    public static Optional<ItemStack> modifyTopSlot(ItemStack top, ItemStack bottom) {
        if (Config.GRINDSTONE_EXTRACT_TREASURE && bottom.isOf(Items.BOOK)) {
            ItemStack disenchantedTool = top.copy();
            Map<Enchantment, Integer> keptEnchantments = extractEnchantments(disenchantedTool, false);
            if (keptEnchantments != null) {
                if (Config.GRINDSTONE_EXTRACT_KEEP_NON_TREASURE) {
                    EnchantmentHelper.set(keptEnchantments, disenchantedTool);
                } else {
                    EnchantmentHelper.set(new LinkedHashMap<>(), disenchantedTool);
                }
                return Optional.of(disenchantedTool);
            }
        }
        return Optional.empty();
    }

    public static Optional<ItemStack> modifyBottomSlot(ItemStack bottom) {
        if (Config.GRINDSTONE_EXTRACT_TREASURE && bottom.isOf(Items.BOOK)) {
            ItemStack book2 = bottom.copy();
            book2.decrement(1);
            return Optional.of(book2.isEmpty() ? ItemStack.EMPTY : book2);
        }
        return Optional.empty();
    }
}
