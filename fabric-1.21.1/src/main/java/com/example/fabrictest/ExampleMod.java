package com.example.fabrictest;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("ExampleMod");

    @Override
    public void onInitialize() {
        LOGGER.info("Hello from a Fabric mod built with ForgeGradle + Mavenizer!");
    }
}
