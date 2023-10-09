package io.github.orlouge.enchantrepair;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class ModifiedGrindstoneHelper {
    public static Map<Enchantment, Integer> filterEnchantments(ItemStack tool, boolean normal, boolean treasure, boolean curse) {
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.get(tool), extracted = new LinkedHashMap<>();
        for (Map.Entry<Enchantment, Integer> enchEntry : enchantments.entrySet()) {
            if (enchEntry.getValue() == 0) continue;
            if (enchEntry.getKey() == Enchantments.VANISHING_CURSE) {
                return null;
            }
            if (enchEntry.getKey().isCursed()) {
                if (curse) extracted.put(enchEntry.getKey(), enchEntry.getValue());
            } else if (enchEntry.getKey().isTreasure()) {
                if (treasure) extracted.put(enchEntry.getKey(), enchEntry.getValue());
            } else {
                if (normal) extracted.put(enchEntry.getKey(), enchEntry.getValue());
            }
        }
        return extracted;
    }

    public static Optional<ItemStack> modifyTopSlot(ItemStack top, ItemStack bottom) {
        if (Config.GRINDSTONE_EXTRACT_TREASURE && bottom.isOf(Items.BOOK)) {
            ItemStack disenchantedTool = top.copy();
            Map<Enchantment, Integer> keptEnchantments = filterEnchantments(disenchantedTool, Config.GRINDSTONE_EXTRACT_KEEP_NON_TREASURE, false, true);
            if (keptEnchantments != null) {
                EnchantmentHelper.set(keptEnchantments, disenchantedTool);
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
