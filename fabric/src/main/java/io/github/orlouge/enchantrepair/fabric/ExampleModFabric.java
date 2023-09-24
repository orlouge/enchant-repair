package io.github.orlouge.enchantrepair.fabric;

import io.github.orlouge.enchantrepair.EnchantRepairMod;
import io.github.orlouge.enchantrepair.ModifiedLootTables;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.loot.LootPool;

import java.util.Collection;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EnchantRepairMod.init();
        LootTableEvents.MODIFY.register(((resourceManager, lootManager, id, tableBuilder, source) -> {
            Collection<LootPool.Builder> pools = ModifiedLootTables.POOLS.get(id);
            if (pools != null) {
                pools.forEach(tableBuilder::pool);
            }
        }));
    }
}
