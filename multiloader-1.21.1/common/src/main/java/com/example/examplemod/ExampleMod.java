package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
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
        // Touch real Minecraft API from shared code. We deliberately call METHODS that get renamed by the
        // production mapping (Mojmap on Forge 1.21.1, intermediary on Fabric) — getPath() and
        // getDescriptionId() — so a broken reobf would throw NoSuchMethodError at runtime.
        ResourceLocation diamondId = ResourceLocation.parse("minecraft:diamond");
        String path = diamondId.getPath();
        String descId = Items.DIAMOND.getDescriptionId();
        LOGGER.info("[{}] Hello from shared common code, running on {}! (id path: {}, item: {})",
            MOD_ID, loaderName, path, descId);
    }
}
