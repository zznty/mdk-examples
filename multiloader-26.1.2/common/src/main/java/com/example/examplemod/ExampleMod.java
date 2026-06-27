package com.example.examplemod;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared, loader-agnostic mod logic. Compiled against clean (official/Mojmap) Minecraft and bundled into each
 * loader jar. Every loader's entrypoint calls {@link #init()}.
 */
public final class ExampleMod {
    public static final String MOD_ID = "multiloader_test";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private ExampleMod() {}

    public static void init(String loaderName) {
        // Touch real Minecraft API from shared code to prove the clean-MC compile classpath works.
        Identifier diamond = Identifier.parse("minecraft:diamond");
        LOGGER.info("[{}] Hello from shared common code, running on {}! (sample MC id: {})",
            MOD_ID, loaderName, diamond);
    }
}
