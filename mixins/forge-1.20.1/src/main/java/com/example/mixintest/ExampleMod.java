package com.example.mixintest;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod("mixin_test")
public class ExampleMod {
    private static final Logger LOGGER = LoggerFactory.getLogger("Mixin Test");

    public ExampleMod() {
        LOGGER.info("[mixin_test] Mod loaded; mixin will fire when the server starts.");
    }
}
