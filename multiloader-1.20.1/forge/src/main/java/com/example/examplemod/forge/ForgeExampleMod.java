package com.example.examplemod.forge;

import com.example.examplemod.ExampleMod;
import net.minecraftforge.fml.common.Mod;

/** Forge entrypoint — delegates straight into the shared common code. */
@Mod(ExampleMod.MOD_ID)
public final class ForgeExampleMod {
    public ForgeExampleMod() {
        ExampleMod.init("Forge");
    }
}
