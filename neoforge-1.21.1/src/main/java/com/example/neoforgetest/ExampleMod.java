package com.example.neoforgetest;

import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("neoforge_1_21_1")
public class ExampleMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("NeoForge Test");

    public ExampleMod() {
        LOGGER.info("Hello from NeoForge via ForgeGradle + NFRT bridge!");
    }
}
