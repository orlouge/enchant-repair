package io.github.orlouge.enchantrepair;


import net.minecraft.util.Identifier;

public class EnchantRepairMod {
    public static final String MOD_ID = "enchantrepair";

    public static final String CONFIG_FNAME = PlatformHelper.getConfigDirectory() + "/" + MOD_ID + ".properties";

    public static void init() {
        Config.load();
        if (Config.LOOT_TABLE_MODIFICATION_DISABLED) {
            ModifiedLootTables.POOLS.clear();
        } else {
            Config.LOOT_TABLE_MODIFICATION_BLACKLIST.forEach(s -> ModifiedLootTables.POOLS.remove(new Identifier(s)));
        }
    }
}
