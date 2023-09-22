package io.github.orlouge.enchantrepair;

import dev.architectury.injectables.annotations.ExpectPlatform;
import java.nio.file.Path;


public class PlatformHelper {
    @ExpectPlatform
    public static Path getConfigDirectory() {
        throw new AssertionError();
    }
}
