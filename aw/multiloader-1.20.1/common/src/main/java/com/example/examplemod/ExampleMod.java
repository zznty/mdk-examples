package com.example.examplemod;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.dedicated.DedicatedServer;
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
        ResourceLocation diamondId = new ResourceLocation("minecraft", "diamond");
        String path = diamondId.getPath();
        String descId = Items.DIAMOND.getDescriptionId();
        LOGGER.info("[{}] Hello from shared common code, running on {}! (id path: {}, item: {})",
            MOD_ID, loaderName, path, descId);
    }

    /// Called by each loader module when the server starts. Casts the server to {@code DedicatedServer} and
    /// calls {@code initServer()} — a {@code protected} method widened to accessible via the Access Widener
    /// file ({@code multiloader_test.accesswidener}). Without the AW this would not compile, because
    /// {@code initServer()} is protected and this class is not a subclass of DedicatedServer.
    ///
    /// On Forge (SRG runtime) the AW is converted to an AT by the Mavenizer and applied during MC
    /// deobfuscation. On Fabric (intermediary runtime) the Fabric Loader consumes the AW natively.
    /// Both paths work because the AW file is in the {@code v2 named} (Mojang) namespace — the same
    /// namespace used during development — and each loader's runtime remaps it as needed.
    public static void testWidenedAccess(DedicatedServer server) {
        try {
            boolean result = server.initServer();
            LOGGER.info("[{}] DedicatedServer.initServer() returned {} — access widener works!", MOD_ID, result);
        } catch (java.io.IOException e) {
            LOGGER.error("[{}] initServer() threw", MOD_ID, e);
        }
    }
}
