package io.github.orlouge.enchantrepair;


public class EnchantRepairMod {
    public static final String MOD_ID = "enchantrepair";

    public static final String CONFIG_FNAME = PlatformHelper.getConfigDirectory() + "/" + MOD_ID + ".properties";

    public static void init() {
        Config.load();
    }
}
