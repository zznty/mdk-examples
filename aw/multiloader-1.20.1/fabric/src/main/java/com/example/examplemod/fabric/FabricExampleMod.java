package com.example.examplemod.fabric;

import com.example.examplemod.ExampleMod;
import net.fabricmc.api.ModInitializer;

/// Fabric entrypoint. The AW test (accessing protected DedicatedServer.initServer()) is verified at COMPILE
/// TIME by the common module's ExampleMod.testWidenedAccess() method — if the AW weren't applied, that method
/// would fail to compile with "initServer() has protected access". At RUNTIME the Fabric Loader reads the
/// accessWidener from fabric.mod.json and applies it, but we don't have a DedicatedServer instance at init time.
@SuppressWarnings("unused")
public final class FabricExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleMod.init("Fabric");
    }
}
