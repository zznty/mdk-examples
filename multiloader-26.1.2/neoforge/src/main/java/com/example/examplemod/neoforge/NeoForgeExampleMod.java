package com.example.examplemod.neoforge;

import com.example.examplemod.ExampleMod;
import net.neoforged.fml.common.Mod;

/** NeoForge entrypoint — delegates straight into the shared common code. */
@Mod(ExampleMod.MOD_ID)
public final class NeoForgeExampleMod {
    public NeoForgeExampleMod() {
        ExampleMod.init("NeoForge");
    }
}
