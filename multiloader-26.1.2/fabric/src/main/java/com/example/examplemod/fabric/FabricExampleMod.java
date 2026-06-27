package com.example.examplemod.fabric;

import com.example.examplemod.ExampleMod;
import net.fabricmc.api.ModInitializer;

/** Fabric entrypoint — delegates straight into the shared common code. */
public final class FabricExampleMod implements ModInitializer {
    @Override
    public void onInitialize() {
        ExampleMod.init("Fabric");
    }
}
