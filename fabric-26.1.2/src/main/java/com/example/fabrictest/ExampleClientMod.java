package com.example.fabrictest;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleClientMod implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExampleMod.MOD_ID + "/client");

    @Override
    public void onInitializeClient() {
        LOGGER.info("[{}] Client init complete.", ExampleMod.MOD_ID);
    }
}
