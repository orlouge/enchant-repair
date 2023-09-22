package io.github.orlouge.enchantrepair.fabric;

import io.github.orlouge.enchantrepair.EnchantRepairMod;
import net.fabricmc.api.ModInitializer;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        EnchantRepairMod.init();
    }
}
