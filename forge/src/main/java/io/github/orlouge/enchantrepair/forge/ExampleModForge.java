package io.github.orlouge.enchantrepair.forge;

import io.github.orlouge.enchantrepair.EnchantRepairMod;
import net.minecraftforge.fml.common.Mod;

@Mod(EnchantRepairMod.MOD_ID)
public class ExampleModForge {
    public ExampleModForge() {
        // Submit our event bus to let architectury register our content on the right time
        EnchantRepairMod.init();
    }
}
