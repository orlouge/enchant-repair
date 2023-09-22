package io.github.orlouge.enchantrepair.forge;

import net.minecraftforge.fml.loading.FMLPaths;
import java.nio.file.Path;

public class PlatformHelperImpl {
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}
