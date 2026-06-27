package com.example.forgetest;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("forge_test")
public class ExampleMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("Forge Test");

    public ExampleMod() {
        LOGGER.info("Hello from Forge via stock ForgeGradle!");
    }
}
