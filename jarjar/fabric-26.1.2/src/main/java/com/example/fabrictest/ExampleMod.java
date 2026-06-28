package com.example.fabrictest;

import com.example.greetlib.Greeter;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleMod implements ModInitializer {
    public static final String MOD_ID = "fabric_test";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Greeter lives in the embedded greetlib jar. With Jar-in-Jar wired for Fabric, greetlib is loaded as a
        // nested mod, so this class resolves at runtime even though greetlib was never installed separately.
        LOGGER.info("[{}] {}", MOD_ID, Greeter.greet("Fabric"));
    }
}
