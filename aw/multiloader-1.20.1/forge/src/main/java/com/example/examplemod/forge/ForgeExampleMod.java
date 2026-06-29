package com.example.examplemod.forge;

import com.example.examplemod.ExampleMod;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;

/// Forge entrypoint. Registers a server-starting listener that calls into the shared common code
/// to test the widened access to [DedicatedServer#initServer()].
@Mod(ExampleMod.MOD_ID)
public final class ForgeExampleMod {
    public ForgeExampleMod() {
        ExampleMod.init("Forge");

        // Register a listener for when the server is about to start. At this point the MinecraftServer
        // instance exists and (on a dedicated server) IS a DedicatedServer, so we can test the widened
        // access to the protected initServer() method.
        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
    }

    private void onServerAboutToStart(ServerAboutToStartEvent event) {
        if (event.getServer() instanceof DedicatedServer server) {
            ExampleMod.testWidenedAccess(server);
        }
    }
}
